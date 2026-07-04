package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic API Response DTO - Wrapper for all API responses
 * Provides consistent response format across the application
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** Response success/failure status */
    private Boolean success;

    /** Response message */
    private String message;

    /** Response data payload */
    private T data;

    /** HTTP status code */
    private Integer statusCode;

    /**
     * Constructor for success response
     */
    public ApiResponse(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Constructor for error response
     */
    public ApiResponse(Boolean success, String message, Integer statusCode) {
        this.success = success;
        this.message = message;
        this.statusCode = statusCode;
    }
}
