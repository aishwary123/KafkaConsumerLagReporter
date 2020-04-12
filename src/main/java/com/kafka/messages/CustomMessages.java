package com.kafka.messages;

/**
 * All the text which is used within the application is listed here. Log
 * messages are not listed here.
 * 
 * @author aishwaryt
 */
public class CustomMessages {

    public static final String UTILITY_CLASS = "Utility class";
    public static final String AVERAGE_LAG_CALCULATION_FAILED = "Average lag calculation failed";
    public static final String EXCEPTION_FOUND = "Exception found";
    public static final String INCORRECT_INPUT_DETAILS = "Incorrect file input details";
    public static final String INCORRECT_CONFIG_FILE_PATH = "Incorrect config file path";
    public static final String LOADING_CONFIG_FAILED = "Loading configuration failed";
    public static final String CONFIG_VALIDATION_FAILED = "Configuration validation failed";
    public static final String CONFIG_NOT_AVAILABLE = "Configuration not available";
    public static final String MBEAN_REGISTRATION_FAILED = "MBean Registratin Failed";
    public static final String INVOKED_CMD_PROCESS = "Invoked cmd process";
    public static final String LAG_NOT_CALCULATED_SO_FAR = "Log not calculated so far";
    public static final String CONNECTION_TO_WAVEFRONT_SERVER_FAILED = "Connection to wavefront server/agent failed";
    public static final String SENDING_DATA_TO_WAVEFRONT_SERVER_FAILED = "Sending data to wavefront server/agent failed";
    public static final String FLUSHING_DATA_TO_WAVEFRONT_SERVER_FAILED = "Flushing data to wavefront server/agent failed";
    public static final String WAVEFRONT_CLIENT_NOT_INITIALIZED = "Wavefront client not initialized";
    public static final String EXCEPTION_DETAILS = "Exception details";
    public static final String WAVEFRONT_CONFIG_MISSING = "Wavefront configuration missing";
    public static final String STARTING_LAG_REPORTING = "Starting Lag reporting";
    public static final String LAG_REPORTED_TO_WAVEFRONT_FOR_GROUP = "Lag reported to wavefront for group";
    public static final String OFFSET_RECORDED_FOR_GROUP = "Offset recorded for group";
    public static final String OFFSET_RECORDED_FOR = "Offset recorded ";
    public static final String STARTED_READING_OFFSET_FOR_GROUP = "Started reading offset for group";
    public static final String FINISHED_READING_OFFSET_FOR_GROUP = "Finished reading offset for group";
    public static final String SYSTEM_VARIABLE_NOT_FOUND = "System variable not found";
    public static final String MESSAGE_PLACEHOLDER = " :{}";

    private CustomMessages() {
        throw new IllegalStateException(CustomMessages.UTILITY_CLASS);
    }

}
