package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.ConsultantRegistrationRequest;
import com.example.vp.consultancy.dto.FarmerRegistrationRequest;
import com.example.vp.consultancy.dto.UserResponse;
import com.example.vp.consultancy.exception.DuplicateResourceException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * Service interface for user management operations.
 * 
 * This service provides methods for:
 * - Consultant registration (admin only)
 * - Farmer registration (consultant only)
 * - User profile retrieval
 * - Farmer list management
 * 
 * Implementations must ensure:
 * - Duplicate mobile/email validation
 * - Password encryption using BCrypt
 * - User profile creation with address
 * - Proper authorization checks
 */
public interface UserService extends UserDetailsService {
    
    /**
     * Registers a new consultant user.
     * 
     * Business logic:
     * 1. Validate consultant registration request
     * 2. Check for duplicate mobile number
     * 3. Check for duplicate email
     * 4. Hash password using BCrypt
     * 5. Create User entity with CONSULTANT role
     * 6. Create UserProfile with email
     * 7. Save all entities to database
     * 8. Return user response DTO
     * 
     * @param request the consultant registration request
     * @return UserResponse containing registered consultant information
     * @throws DuplicateResourceException if mobile or email already exists
     * @throws IllegalArgumentException if request is invalid
     */
    UserResponse registerConsultant(ConsultantRegistrationRequest request);
    
    /**
     * Registers a new farmer user under a specific consultant.
     * 
     * Business logic:
     * 1. Validate farmer registration request
     * 2. Verify consultant exists
     * 3. Check for duplicate mobile number
     * 4. Check for duplicate email
     * 5. Hash password using BCrypt
     * 6. Create User entity with FARMER role
     * 7. Create UserProfile with email and consultant reference
     * 8. Create Address from registration data
     * 9. Save all entities to database
     * 10. Return user response DTO
     * 
     * @param request the farmer registration request
     * @param consultantId the ID of the consultant registering this farmer
     * @return UserResponse containing registered farmer information
     * @throws DuplicateResourceException if mobile or email already exists
     * @throws ResourceNotFoundException if consultant doesn't exist
     * @throws IllegalArgumentException if request is invalid
     */
    UserResponse registerFarmer(FarmerRegistrationRequest request, Long consultantId);
    
    /**
     * Retrieves a user by their ID.
     * 
     * @param userId the ID of the user to retrieve
     * @return UserResponse containing user information
     * @throws ResourceNotFoundException if user doesn't exist
     */
    UserResponse getUserById(Long userId);
    
    /**
     * Retrieves all farmers associated with a specific consultant.
     * 
     * @param consultantId the ID of the consultant
     * @return List of UserResponse DTOs for all farmers under this consultant
     * @throws ResourceNotFoundException if consultant doesn't exist
     */
    List<UserResponse> getAllFarmers(Long consultantId);
    
    /**
     * Retrieves the currently authenticated user.
     * 
     * @return UserResponse containing current user information
     * @throws ResourceNotFoundException if user not found
     */
    UserResponse getCurrentUser();
}
