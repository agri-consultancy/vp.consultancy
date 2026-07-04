package com.example.vp.consultancy.exception;

/**
 * Exception thrown when a resource is not found
 * @author VP Consultancy Team
 * @version 1.0
 */
public class ResourceNotFoundException extends VPException {

    /**
     * Constructor
     * @param message Error message
     */
    public ResourceNotFoundException(String message) {
        super(message, 404, "RESOURCE_NOT_FOUND");
    }
}
