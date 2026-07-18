package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.LoginRequest;
import com.example.vp.consultancy.dto.LoginResponse;
import com.example.vp.consultancy.dto.RefreshTokenRequest;
import com.example.vp.consultancy.exception.InvalidCredentialsException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;

/**
 * Service interface for handling user authentication operations.
 * 
 * This service provides methods for:
 * - User login with credentials validation
 * - Token refresh for extending user sessions
 * - User logout and session cleanup
 * 
 * Implementations must ensure:
 * - Proper credential validation
 * - JWT token generation and management
 * - Secure session handling
 * - Null checks and validation
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user with their credentials.
     * 
     * This method performs the following operations:
     * 1. Validates the login request parameters (not null, properly formatted)
     * 2. Authenticates the user against the database using provided credentials
     * 3. Generates JWT access token for authenticated user
     * 4. Creates and stores refresh token for session extension
     * 5. Returns login response with tokens and user information
     * 
     * @param request the login request containing mobile number and password
     * @return LoginResponse containing access token, refresh token, and user details
     * @throws InvalidCredentialsException if credentials don't match any user
     * @throws IllegalArgumentException if request is null or missing required fields
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * Refreshes the user's access token using a valid refresh token.
     * 
     * This method performs the following operations:
     * 1. Validates the refresh token request parameters
     * 2. Retrieves the refresh token from the database
     * 3. Verifies that the token hasn't expired
     * 4. Loads the associated user
     * 5. Generates a new access token
     * 6. Returns the new access token in response
     * 
     * @param request the refresh token request containing the refresh token
     * @return LoginResponse containing the new access token
     * @throws ResourceNotFoundException if refresh token doesn't exist
     * @throws InvalidCredentialsException if refresh token has expired
     * @throws IllegalArgumentException if request is null or missing token
     */
    LoginResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * Logs out a user by clearing their session and refresh tokens.
     * 
     * This method performs the following operations:
     * 1. Validates that userId is not null
     * 2. Deletes all refresh tokens associated with the user
     * 3. Clears any session information
     * 4. Optionally invalidates any active sessions
     * 
     * @param userId the ID of the user to logout
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws IllegalArgumentException if userId is null or invalid
     */
    void logout(Long userId);

    /**
     * Changes password for the currently authenticated user.
     * @param request contains oldPassword and newPassword
     */
    void changePassword(com.example.vp.consultancy.dto.UpdatePasswordRequest request);
}
