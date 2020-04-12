package com.kafka.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kafka.configs.CommonConfig;
import com.kafka.exceptions.WavefrontClientException;
import com.kafka.messages.CustomMessages;
import com.kafka.models.OffsetRecord;
import com.kafka.utils.WFClient;

public class LagWFReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(
                LagWFReporter.class);

    public static final String METRIC_KEY = "kafka.offsets.consumer.lag";
    WFClient wfClient;

    public LagWFReporter(final String wavefrontProxyUrl,
                         final int wavefrontProxyPort)
        throws WavefrontClientException {
        wfClient = new WFClient(wavefrontProxyUrl, wavefrontProxyPort);
    }

    public void reportOffsetRecord(List<OffsetRecord> offsetRecords)
        throws WavefrontClientException {

        Properties configProperties = CommonConfig.getConfiguration();
        String wavefrontReporterPrefix = configProperties.getProperty(
                    CommonConfig.WAVEFRONT_REPORTER_PREFIX);
        final String absoluteMetricKey = wavefrontReporterPrefix.concat(
                    ".").concat(METRIC_KEY);

        offsetRecords.stream().forEach(record -> {
            LOGGER.info(CustomMessages.OFFSET_RECORDED_FOR.concat(
                        CustomMessages.MESSAGE_PLACEHOLDER), record);

            Map<String, String> pointTags = new HashMap<>();
            pointTags.put("group", record.getGroup());
            pointTags.put("partition", record.getPartition());
            pointTags.put("topic", record.getTopic());

            try {

                wfClient.saveStats(absoluteMetricKey, record.getLag(),
                            InetAddress.getLocalHost().getHostName(),
                            pointTags);
            } catch (UnknownHostException
                        | WavefrontClientException exception) {
                LOGGER.error(
                            CustomMessages.SENDING_DATA_TO_WAVEFRONT_SERVER_FAILED.concat(
                                        CustomMessages.MESSAGE_PLACEHOLDER),
                            exception);

            }
        });
        wfClient.flushClient();
    }
}
