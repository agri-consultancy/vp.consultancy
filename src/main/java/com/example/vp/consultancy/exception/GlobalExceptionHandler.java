package com.example.vp.consultancy.exception;

import com.example.vp.consultancy.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler - Centralized exception handling for the entire application
 * Provides consistent error response format across all endpoints
 * @author VP Consultancy Team
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle VPException - Custom application exceptions
     */
    @ExceptionHandler(VPException.class)
    public ResponseEntity<ApiResponse<Object>> handleVPException(VPException ex, WebRequest request) {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            ex.getMessage(),
            ex.getStatusCode()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatusCode()));
    }

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            ex.getMessage(),
            404
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle InvalidCredentialsException
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            ex.getMessage(),
            401
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            ex.getMessage(),
            409
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle ForbiddenException
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            ex.getMessage(),
            403
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Spring Security access denials.
     * Expired or missing tokens should return 401, authenticated role violations stay 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean unauthenticated = authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;

        int statusCode = unauthenticated ? 401 : 403;
        HttpStatus status = unauthenticated ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        String message = unauthenticated ? "Unauthorized" : "Access denied";

        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            message,
            statusCode
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle RateLimitExceededException
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleRateLimitExceededException(
            RateLimitExceededException ex, WebRequest request) {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            ex.getMessage(),
            429
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            "Validation failed",
            errors
        );
        errorResponse.setStatusCode(400);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
            false,
            "An unexpected error occurred: " + ex.getMessage(),
            500
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
