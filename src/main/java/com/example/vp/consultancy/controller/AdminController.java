package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.annotation.RateLimit;
import com.example.vp.consultancy.dto.ApiResponse;
import com.example.vp.consultancy.dto.ConsultantRegistrationRequest;
import com.example.vp.consultancy.dto.UserResponse;
import com.example.vp.consultancy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for admin operations.
 * 
 * Provides endpoints for administrative tasks including:
 * - Consultant registration
 * - User management
 * 
 * All endpoints are secured with @PreAuthorize("hasRole('ADMIN')")
 * and require valid JWT authentication.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;

    /**
     * Constructor with dependency injection.
     * 
     * @param userService user management service
     */
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new consultant user.
     * 
     * Rate limited to 10 requests per 60 seconds to prevent abuse.
     * Only accessible to users with ADMIN role.
     * 
     * Business Logic:
     * 1. Receives consultant registration details from client
     * 2. Validates consultant doesn't already exist (mobile/email check)
     * 3. Creates User entity with CONSULTANT role
     * 4. Creates associated UserProfile
     * 5. Returns consultant information in response
     * 
     * @param request the consultant registration request with mobile, email, name, and password
     * @return ResponseEntity with ApiResponse containing registered consultant information
     * @throws DuplicateResourceException if mobile or email already registered
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    @PostMapping("/consultants")
    @RateLimit(limit = 10, windowSize = 60)
    public ResponseEntity<ApiResponse<UserResponse>> registerConsultant(
            @Valid @RequestBody ConsultantRegistrationRequest request) {
        UserResponse response = userService.registerConsultant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(
                true,
                "Consultant registered successfully",
                response
            ));
    }
}
