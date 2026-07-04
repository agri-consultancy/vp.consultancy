package com.example.vp.consultancy.exception;

/**
 * Exception thrown when rate limit is exceeded
 * @author VP Consultancy Team
 * @version 1.0
 */
public class RateLimitExceededException extends VPException {

    /**
     * Constructor
     * @param message Error message
     */
    public RateLimitExceededException(String message) {
        super(message, 429, "RATE_LIMIT_EXCEEDED");
    }
}
