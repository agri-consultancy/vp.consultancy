package com.example.vp.consultancy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for rate limiting specific endpoints.
 * This annotation is processed by the RateLimitingInterceptor to enforce
 * rate limiting policies on individual endpoints.
 * 
 * Example usage:
 * @RateLimit(limit = 5, windowSize = 60)
 * public ResponseEntity<?> loginUser(LoginRequest request) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Maximum number of requests allowed within the window.
     * Default is 10 requests.
     * 
     * @return the maximum number of requests
     */
    int limit() default 10;
    
    /**
     * Time window size in seconds during which requests are counted.
     * Default is 60 seconds (1 minute).
     * 
     * @return the window size in seconds
     */
    int windowSize() default 60;
}
