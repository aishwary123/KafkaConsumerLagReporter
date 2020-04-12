package com.kafka.exceptions;

public class WavefrontClientException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String message;

    public WavefrontClientException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
