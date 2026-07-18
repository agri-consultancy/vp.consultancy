package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.annotation.RateLimit;
import com.example.vp.consultancy.dto.ApiResponse;
import com.example.vp.consultancy.dto.LoginRequest;
import com.example.vp.consultancy.dto.LoginResponse;
import com.example.vp.consultancy.dto.RefreshTokenRequest;
import com.example.vp.consultancy.dto.UpdatePasswordRequest;
import com.example.vp.consultancy.dto.ConsultantRegistrationRequest;
import com.example.vp.consultancy.dto.UserResponse;
import com.example.vp.consultancy.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.vp.consultancy.exception.InvalidCredentialsException;
import com.example.vp.consultancy.exception.RateLimitExceededException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for authentication endpoints.
 * 
 * Provides endpoints for:
 * - User login with credentials
 * - Token refresh with refresh token
 * - User logout
 * 
 * All login and refresh endpoints have rate limiting applied
 * to prevent brute force attacks and DoS attacks.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Constructor with dependency injection.
     * 
     * @param authenticationService authentication service for credential validation
     */
    public AuthController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    /**
     * Login endpoint that authenticates user with mobile and password.
     * 
     * Rate limited to 5 requests per 60 seconds to prevent brute force attacks.
     * 
     * Business Logic:
     * 1. Receives login credentials from client
     * 2. Validates credentials against the database
     * 3. Generates JWT access token and refresh token
     * 4. Returns tokens in response
     * 
     * @param request the login request containing mobile and password
     * @return ResponseEntity with ApiResponse containing login response with tokens
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    @PostMapping("/login")
    @RateLimit(limit = 5, windowSize = 60)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(
            true, 
            "Login successful",
            response,
            HttpStatus.OK.value()
        ));
    }

    /**
     * Refresh token endpoint that issues a new access token.
     * 
     * Rate limited to 10 requests per 60 seconds.
     * 
     * Business Logic:
     * 1. Receives refresh token from client
     * 2. Validates refresh token against the database
     * 3. Generates new access token if refresh token is valid
     * 4. Returns new access token in response
     * 
     * @param request the refresh token request containing the refresh token
     * @return ResponseEntity with ApiResponse containing new access token
     * @throws InvalidCredentialsException if refresh token is invalid or expired
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    @PostMapping("/refresh")
    @RateLimit(limit = 10, windowSize = 60)
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Token refreshed successfully",
            response,
            HttpStatus.OK.value()
        ));
    }

    /**
     * Logout endpoint that invalidates user's refresh tokens.
     * 
     * Business Logic:
     * 1. Receives userId from path variable
     * 2. Deletes all refresh tokens for this user
     * 3. Invalidates all active sessions
     * 
     * @param userId the ID of the user to logout
     * @return ResponseEntity with ApiResponse containing success message
     * @throws ResourceNotFoundException if user doesn't exist
     */
    @PostMapping("/logout/{userId}")
    public ResponseEntity<ApiResponse<Void>> logout(@PathVariable Long userId) {
        authenticationService.logout(userId);
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Logout successful",
            null,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Change password for authenticated user.
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authenticationService.changePassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password updated successfully", null, HttpStatus.OK.value()));
    }

    /**
     * Public endpoint to create an admin user. No auth required.
     * This is intentionally exposed without auth checks to allow initial bootstrap.
     */
    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(@Valid @RequestBody ConsultantRegistrationRequest request) {
        UserResponse response = userService.createAdmin(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Admin created successfully", response, HttpStatus.OK.value()));
    }

    /**
     * Returns the full details for the current user based on the access token.
     */
    @GetMapping("/user-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.example.vp.consultancy.dto.UserDetailsResponse>> getCurrentUserDetails() {
        com.example.vp.consultancy.dto.UserDetailsResponse details = userService.getCurrentUserDetails();
        return ResponseEntity.ok(new ApiResponse<>(true, "User details retrieved successfully", details, HttpStatus.OK.value()));
    }
}
