package com.kafka.configs;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.validation.ValidationException;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kafka.exceptions.ConfigException;
import com.kafka.messages.CustomMessages;

public class CommonConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(
                CommonConfig.class);

    private static final String CONFIG_FILE_PATH = "CONFIG_FILE_PATH";
    public static final String GROUPS = "GROUPS";
    public static final String BOOTSTRAP_SERVER = "BOOTSTRAP_SERVER";
    public static final String KAFKA_LOCATION = "KAFKA_LOCATION";
    public static final String PERIOD = "PERIOD";
    public static final String WAVEFRONT_REPORTING_ENABLED = "WAVEFRONT_REPORTING_ENABLED";
    public static final String WAVEFRONT_REPORTER_PREFIX = "WAVEFRONT_REPORTER_PREFIX";
    public static final String WAVEFRONT_HOST = "WAVEFRONT_HOST";
    public static final String WAVEFRONT_PORT = "WAVEFRONT_PORT";

    private static Properties properties;

    private CommonConfig() {
        throw new IllegalStateException(CustomMessages.UTILITY_CLASS);
    }

    public static Properties getConfiguration() {

        if (null == properties) {
            throw new ConfigException(CustomMessages.CONFIG_NOT_AVAILABLE);
        }
        return properties;
    }

    /**
     * It will load the configuration from file whose path is specified in
     * {@literal CONFIG_FILE_PATH} environment variable. It will load the
     * configuration from environment variable also. Value from environment
     * variable is given more preference.
     * 
     * @return {@code Properties} object with configuration information.
     */
    public static Properties loadConfiguration() {
        properties = new Properties();
        loadConfigurationFromFile();
        loadConfigurationFromEnv();
        validateConfiguration();
        return properties;
    }

    private static void loadConfigurationFromFile() {

        final String configFileLocation = System.getenv(CONFIG_FILE_PATH);
        if (Strings.isEmpty(configFileLocation)
                    || !Paths.get(configFileLocation).toFile().exists()) {

            LOGGER.info(CustomMessages.INCORRECT_INPUT_DETAILS.concat(
                        CustomMessages.MESSAGE_PLACEHOLDER),
                        CustomMessages.INCORRECT_CONFIG_FILE_PATH);

        }

        Path configFilePath = Paths.get(configFileLocation);

        try (FileInputStream fileInputStream = new FileInputStream(
                    configFilePath.toFile())) {
            properties.load(fileInputStream);
        } catch (Exception exception) {
            LOGGER.error(
                        CustomMessages.LOADING_CONFIG_FAILED.concat(
                                    CustomMessages.MESSAGE_PLACEHOLDER),
                        exception.getMessage());
            throw new ConfigException(CustomMessages.LOADING_CONFIG_FAILED);
        }

    }

    private static void loadConfigurationFromEnv() {

        if (Strings.isNotEmpty(System.getenv(GROUPS))) {
            properties.setProperty(GROUPS, System.getenv(GROUPS));
        }
        if (Strings.isNotEmpty(System.getenv(BOOTSTRAP_SERVER))) {
            properties.setProperty(BOOTSTRAP_SERVER,
                        System.getenv(BOOTSTRAP_SERVER));
        }
        if (Strings.isNotEmpty(System.getenv(KAFKA_LOCATION))) {
            properties.setProperty(KAFKA_LOCATION,
                        System.getenv(KAFKA_LOCATION));
        }
        if (Strings.isNotEmpty(System.getenv(PERIOD))) {
            properties.setProperty(PERIOD, System.getenv(PERIOD));
        }
        if (Strings.isNotEmpty(System.getenv(WAVEFRONT_REPORTING_ENABLED))) {
            properties.setProperty(WAVEFRONT_REPORTING_ENABLED,
                        System.getenv(WAVEFRONT_REPORTING_ENABLED));
        }
        if (Strings.isNotEmpty(System.getenv(WAVEFRONT_REPORTER_PREFIX))) {
            properties.setProperty(WAVEFRONT_REPORTER_PREFIX,
                        System.getenv(WAVEFRONT_REPORTER_PREFIX));
        }
        if (Strings.isNotEmpty(System.getenv(WAVEFRONT_HOST))) {
            properties.setProperty(WAVEFRONT_HOST,
                        System.getenv(WAVEFRONT_HOST));
        }
        if (Strings.isNotEmpty(System.getenv(WAVEFRONT_PORT))) {
            properties.setProperty(WAVEFRONT_PORT,
                        System.getenv(WAVEFRONT_PORT));
        }

    }

    private static void validateConfiguration() {

        if (!(properties.containsKey(GROUPS)
                    && properties.containsKey(BOOTSTRAP_SERVER)
                    && properties.containsKey(KAFKA_LOCATION)
                    && properties.containsKey(PERIOD)
                    && properties.containsKey(WAVEFRONT_REPORTING_ENABLED))) {

            throw new ValidationException(
                        CustomMessages.CONFIG_VALIDATION_FAILED);
        }
        if (Boolean.valueOf(properties.getProperty(WAVEFRONT_REPORTING_ENABLED))
                    && !(properties.containsKey(WAVEFRONT_HOST)
                                || properties.containsKey(WAVEFRONT_PORT)
                                || properties.containsKey(
                                            WAVEFRONT_REPORTER_PREFIX))) {
            LOGGER.error(CustomMessages.WAVEFRONT_CONFIG_MISSING);
            throw new ValidationException(
                        CustomMessages.CONFIG_VALIDATION_FAILED.concat(
                                    ":").concat(CustomMessages.WAVEFRONT_CONFIG_MISSING));
        }

    }
}
