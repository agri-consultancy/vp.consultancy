package com.example.vp.consultancy.exception;

/**
 * Exception thrown when access is forbidden/unauthorized
 * @author VP Consultancy Team
 * @version 1.0
 */
public class ForbiddenException extends VPException {

    /**
     * Constructor
     * @param message Error message
     */
    public ForbiddenException(String message) {
        super(message, 403, "FORBIDDEN");
    }
}
