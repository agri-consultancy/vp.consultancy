package com.example.vp.consultancy.exception;

import lombok.Getter;

/**
 * Custom exception for application business logic errors
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
public class VPException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    /**
     * Constructor with message and status code
     * @param message Error message
     * @param statusCode HTTP status code
     * @param errorCode Error code for client handling
     */
    public VPException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message and status code
     * @param message Error message
     * @param statusCode HTTP status code
     */
    public VPException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = "ERROR";
    }
}
