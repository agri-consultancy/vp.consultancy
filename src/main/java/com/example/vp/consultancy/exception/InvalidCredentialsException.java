package com.example.vp.consultancy.exception;

/**
 * Exception thrown when authentication fails
 * @author VP Consultancy Team
 * @version 1.0
 */
public class InvalidCredentialsException extends VPException {

    /**
     * Constructor
     * @param message Error message
     */
    public InvalidCredentialsException(String message) {
        super(message, 401, "INVALID_CREDENTIALS");
    }
}
