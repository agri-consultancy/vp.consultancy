package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.annotation.RateLimit;
import com.example.vp.consultancy.dto.ApiResponse;
import com.example.vp.consultancy.dto.FarmerRegistrationRequest;
import com.example.vp.consultancy.dto.UserResponse;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for consultant operations.
 * 
 * Provides endpoints for consultant-specific tasks including:
 * - Farmer registration under consultant
 * - Farmer list retrieval
 * 
 * All endpoints are secured with @PreAuthorize("hasRole('CONSULTANT')")
 * and require valid JWT authentication.
 */
@RestController
@RequestMapping("/api/consultant")
@PreAuthorize("hasRole('CONSULTANT')")
public class ConsultantController {
    
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Constructor with dependency injection.
     * 
     * @param userService user management service
     * @param userRepository user repository for database operations
     */
    public ConsultantController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new farmer user under the current consultant.
     * 
     * Rate limited to 20 requests per 60 seconds.
     * Only accessible to users with CONSULTANT role.
     * 
     * Business Logic:
     * 1. Extracts current authenticated consultant from SecurityContext
     * 2. Receives farmer registration details from client
     * 3. Validates farmer doesn't already exist (mobile/email check)
     * 4. Creates User entity with FARMER role
     * 5. Links farmer to current consultant
     * 6. Creates associated UserProfile and Address
     * 7. Returns farmer information in response
     * 
     * @param request the farmer registration request with mobile, email, name, password, and address
     * @return ResponseEntity with ApiResponse containing registered farmer information
     * @throws DuplicateResourceException if mobile or email already registered
     * @throws ResourceNotFoundException if current consultant not found
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    @PostMapping("/farmers")
    @RateLimit(limit = 20, windowSize = 60)
    public ResponseEntity<ApiResponse<UserResponse>> registerFarmer(
            @Valid @RequestBody FarmerRegistrationRequest request) {
        // Extract current authenticated consultant's mobile
        String consultantMobile = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
        
        // Retrieve consultant user to get the ID
        User consultant = userRepository.findByMobile(consultantMobile)
            .orElseThrow(() -> new RuntimeException("Consultant not found"));
        
        // Register farmer under this consultant
        UserResponse response = userService.registerFarmer(request, consultant.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(
                true,
                "Farmer registered successfully",
                response
            ));
    }

    /**
     * Retrieves all farmers registered under the current consultant.
     * 
     * Only accessible to users with CONSULTANT role.
     * Read-only operation.
     * 
     * Business Logic:
     * 1. Extracts current authenticated consultant from SecurityContext
     * 2. Retrieves all farmers associated with this consultant
     * 3. Returns list of farmer information
     * 
     * @return ResponseEntity with ApiResponse containing list of farmers
     * @throws ResourceNotFoundException if current consultant not found
     */
    @GetMapping("/farmers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllFarmers() {
        // Extract current authenticated consultant's mobile
        String consultantMobile = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
        
        // Retrieve consultant user to get the ID
        User consultant = userRepository.findByMobile(consultantMobile)
            .orElseThrow(() -> new RuntimeException("Consultant not found"));
        
        // Get all farmers for this consultant
        List<UserResponse> farmers = userService.getAllFarmers(consultant.getId());
        
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Farmers retrieved successfully",
            farmers
        ));
    }
}
