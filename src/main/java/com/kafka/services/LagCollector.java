package com.kafka.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kafka.configs.CommonConfig;
import com.kafka.exceptions.LagReportingException;
import com.kafka.exceptions.WavefrontClientException;
import com.kafka.messages.CustomMessages;
import com.kafka.models.OffsetRecord;
import com.kafka.models.Offsets;

public class LagCollector {

    public void startDataCollection() {

        Properties configProperties = CommonConfig.getConfiguration();
        String groups = configProperties.getProperty(CommonConfig.GROUPS);
        String period = configProperties.getProperty(CommonConfig.PERIOD);
        String[] groupsArr = groups.split("[|]");
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
                    groupsArr.length);
        for (String group : groupsArr) {
            scheduledExecutorService.scheduleWithFixedDelay(
                        new Scheduler(group), 0, Long.parseLong(period),
                        TimeUnit.SECONDS);
        }
    }
}

class Scheduler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
                Scheduler.class);

    private String group;
    private Offsets mbean;

    public Scheduler(final String group) {
        this.group = group;
        mbean = new Offsets();
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName name = new ObjectName(
                        "kafka.offsets." + group + ":type=Offsets");
            mbeanServer.registerMBean(mbean, name);
        } catch (MalformedObjectNameException | NotCompliantMBeanException
                    | MBeanRegistrationException
                    | InstanceAlreadyExistsException exception) {
            LOGGER.error(
                        CustomMessages.MBEAN_REGISTRATION_FAILED.concat(
                                    CustomMessages.MESSAGE_PLACEHOLDER),
                        exception);
            throw new LagReportingException(
                        CustomMessages.MBEAN_REGISTRATION_FAILED);
        }
    }

    @Override
    public void run() {

        final List<OffsetRecord> offsetRecords = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();

        try {

            String kafkaLocation = CommonConfig.getConfiguration().getProperty(
                        CommonConfig.KAFKA_LOCATION);
            String bootstrapServer = CommonConfig.getConfiguration().getProperty(
                        CommonConfig.BOOTSTRAP_SERVER);
            String commandLocation = kafkaLocation.concat(
                        File.separator).concat("bin/kafka-consumer-groups.sh");
            String consumerPropertiesLocation = kafkaLocation.concat(
                        File.separator).concat("consumer.properties");
            String command = commandLocation.concat(
                        " --command-config ").concat(
                                    consumerPropertiesLocation).concat(
                                                " --describe --group ").concat(
                                                            group).concat(
                                                                        " --bootstrap-server ").concat(
                                                                                    bootstrapServer);

            final Process process = runtime.exec(command);

            if (LOGGER.isInfoEnabled())
                LOGGER.info(CustomMessages.INVOKED_CMD_PROCESS.concat(command));

            try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                String consoleLine = null;
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(CustomMessages.STARTED_READING_OFFSET_FOR_GROUP.concat(
                                group));
                while ((consoleLine = bufferedReader.readLine()) != null) {
                    // GROUP, TOPIC, PARTITION, CURRENT-OFFSET,
                    // LOG-END-OFFSET, LAG, CONSUMER-ID, HOST, CLIENT-ID
                    // SKIPPING THE HEADER LINE
                    if (consoleLine.contains("GROUP, TOPIC, PARTITION")) {
                        continue;
                    }

                    String[] splitted = consoleLine.split("\\s+");

                    if (splitted.length == 9) {
                        OffsetRecord offsetRecord = collect(splitted);
                        if (null != offsetRecord)
                            offsetRecords.add(offsetRecord);
                    }
                }
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(CustomMessages.FINISHED_READING_OFFSET_FOR_GROUP.concat(
                                group));
                mbean.setOffsetRecords(offsetRecords);
                if (!offsetRecords.isEmpty())
                    reportMetricsToWavefront(offsetRecords);
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(CustomMessages.OFFSET_RECORDED_FOR_GROUP.concat(
                                CustomMessages.MESSAGE_PLACEHOLDER), group);

            } catch (Exception exception) {
                LOGGER.error(
                            CustomMessages.EXCEPTION_FOUND.concat(
                                        CustomMessages.MESSAGE_PLACEHOLDER),
                            exception);
                mbean.recordError();
            }

            int waited = process.waitFor();
            if (waited != 0) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Exception found. Status of waited: ".concat(
                                String.valueOf(waited)));
                mbean.recordError();
            }

        } catch (Exception exception) {
            LOGGER.error(
                        CustomMessages.EXCEPTION_FOUND.concat(
                                    CustomMessages.MESSAGE_PLACEHOLDER),
                        exception);
            mbean.recordError();
        }

    }

    private void reportMetricsToWavefront(final List<OffsetRecord> offsetRecords) {

        Properties configProperties = CommonConfig.getConfiguration();
        String wavefrontReportingEnabled = configProperties.getProperty(
                    CommonConfig.WAVEFRONT_REPORTING_ENABLED);
        if (Boolean.valueOf(wavefrontReportingEnabled)) {
            String wavefrontHost = configProperties.getProperty(
                        CommonConfig.WAVEFRONT_HOST);
            String wavefrontPort = configProperties.getProperty(
                        CommonConfig.WAVEFRONT_PORT);

            try {
                LagWFReporter lagWFReporter = new LagWFReporter(wavefrontHost,
                            Integer.parseInt(wavefrontPort));
                lagWFReporter.reportOffsetRecord(offsetRecords);
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(CustomMessages.LAG_REPORTED_TO_WAVEFRONT_FOR_GROUP.concat(
                                CustomMessages.MESSAGE_PLACEHOLDER), group);

            } catch (NumberFormatException
                        | WavefrontClientException exception) {
                LOGGER.error(
                            CustomMessages.SENDING_DATA_TO_WAVEFRONT_SERVER_FAILED.concat(
                                        CustomMessages.MESSAGE_PLACEHOLDER).concat(
                                                    exception.getMessage()));
            }

        }

    }

    private OffsetRecord collect(String[] splitted) {
        // GROUP, TOPIC, PARTITION, CURRENT-OFFSET,
        // LOG-END-OFFSET, LAG, CONSUMER-ID, HOST, CLIENT-ID

        String groupName = splitted[0].trim();
        String topicName = splitted[1].trim();
        String partitionName = splitted[2].trim();
        long lag = 0;
        try {
            lag = Long.parseLong(splitted[5].trim());
        } catch (NumberFormatException numberFormatException) {
            // SOMETIME LAG NOT SPECIFIED PROPERLY ON THE CONSOLE.
            // IT IS REPORTED AS '-'
            return null;
        }
        OffsetRecord record = new OffsetRecord();
        record.setGroup(groupName);
        record.setTopic(topicName);
        record.setPartition(partitionName);
        record.setLag(lag);
        return record;
    }

}
