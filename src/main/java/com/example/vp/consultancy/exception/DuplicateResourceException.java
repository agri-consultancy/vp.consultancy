package com.example.vp.consultancy.exception;

/**
 * Exception thrown when a resource already exists
 * @author VP Consultancy Team
 * @version 1.0
 */
public class DuplicateResourceException extends VPException {

    /**
     * Constructor
     * @param message Error message
     */
    public DuplicateResourceException(String message) {
        super(message, 409, "DUPLICATE_RESOURCE");
    }
}
