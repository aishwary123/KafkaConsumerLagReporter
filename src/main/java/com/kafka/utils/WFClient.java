package com.kafka.utils;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kafka.exceptions.WavefrontClientException;
import com.kafka.messages.CustomMessages;
import com.wavefront.integrations.Wavefront;

public class WFClient {

    Wavefront wavefront;

    private static final Logger LOGGER = LoggerFactory.getLogger(
                WFClient.class);

    private void connect()
        throws WavefrontClientException {
        try {
            if (null == wavefront) {
                throw new WavefrontClientException(
                            CustomMessages.WAVEFRONT_CLIENT_NOT_INITIALIZED);
            }
            if (!wavefront.isConnected())
                wavefront.connect();
        } catch (IllegalStateException | IOException exception) {
            LOGGER.error(
                        CustomMessages.EXCEPTION_DETAILS.concat(
                                    CustomMessages.MESSAGE_PLACEHOLDER),
                        exception.getMessage());
            throw new WavefrontClientException(
                        CustomMessages.CONNECTION_TO_WAVEFRONT_SERVER_FAILED);
        }
    }

    /**
     * It creates a wavefront client object for the wavefront server/agent whose
     * details is passed in parameter.Using this wavefront client we can report
     * custom metrics to wavefront.
     * 
     * @param wfProxyIp
     * @param wfProxyPort
     * @throws WavefrontClientException
     */
    public WFClient(final String wfProxyIp,
                    final int wfProxyPort)
        throws WavefrontClientException {
        wavefront = new Wavefront(wfProxyIp, wfProxyPort);
        connect();
    }

    /**
     * This method is used to send the metrics to wavefront server/agent.
     * 
     * @param metricName
     * @param metricValue
     * @param source
     * @param pointTags
     * @throws WavefrontClientException
     */
    public void saveStats(final String metricName,
                          final double metricValue,
                          final String source,
                          final Map<String, String> pointTags)
        throws WavefrontClientException {
        try {
            connect();
            wavefront.send(metricName, metricValue, source, pointTags);
        } catch (IOException ioException) {
            LOGGER.error(
                        CustomMessages.EXCEPTION_DETAILS.concat(
                                    CustomMessages.MESSAGE_PLACEHOLDER),
                        ioException.getMessage());
            throw new WavefrontClientException(
                        CustomMessages.SENDING_DATA_TO_WAVEFRONT_SERVER_FAILED);

        }
    }

    /**
     * This method flushes the data present in it to server/agent.
     * 
     * @throws WavefrontClientException
     */
    public void flushClient()
        throws WavefrontClientException {
        try {
            if (null == wavefront) {
                throw new WavefrontClientException(
                            CustomMessages.WAVEFRONT_CLIENT_NOT_INITIALIZED);
            }
            wavefront.flush();
        } catch (IOException ioException) {
            LOGGER.error(
                        CustomMessages.EXCEPTION_DETAILS.concat(
                                    CustomMessages.MESSAGE_PLACEHOLDER),
                        ioException.getMessage());
            throw new WavefrontClientException(
                        CustomMessages.FLUSHING_DATA_TO_WAVEFRONT_SERVER_FAILED);
        }
    }
}