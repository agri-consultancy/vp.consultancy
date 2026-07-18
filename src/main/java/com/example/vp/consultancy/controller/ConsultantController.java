package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.annotation.RateLimit;
import com.example.vp.consultancy.dto.*;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.exception.DuplicateResourceException;
import com.example.vp.consultancy.exception.RateLimitExceededException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.service.*;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
public class  ConsultantController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final CropService cropService;
    private final CropVarietyService cropVarietyService;
    private final MasterScheduleService masterScheduleService;
    private final UserProfileRepository userProfileRepository;
    private final ConsultantAdvertisementService advertisementService;
    private final SendScheduleService sendScheduleService;


    /**
     * Constructor with dependency injection.
     * 
     * @param userService user management service
     * @param userRepository user repository for database operations
     * @param advertisementService consultant advertisement service
     */
    public ConsultantController(UserService userService, UserRepository userRepository, CropService cropService,
                                CropVarietyService cropVarietyService, MasterScheduleService masterScheduleService,
                                UserProfileRepository userProfileRepository, ConsultantAdvertisementService advertisementService, SendScheduleService sendScheduleService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.cropService = cropService;
        this.cropVarietyService = cropVarietyService;
        this.masterScheduleService = masterScheduleService;
        this.userProfileRepository = userProfileRepository;
        this.advertisementService = advertisementService;
        this.sendScheduleService = sendScheduleService;
    }

    private Long getCurrentConsultantProfileId() {
        String consultantMobile = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        UserProfile consultantProfile = userProfileRepository.findByUser_Mobile(consultantMobile)
            .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));

        return consultantProfile.getId();
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
                response,
                HttpStatus.OK.value()
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
            farmers,
            HttpStatus.OK.value()
        ));
    }


    /**
     * Retrieves all crops sorted by name.
     *
     * Accessible to all authenticated users.
     *
     * @return ResponseEntity with ApiResponse containing list of all crops
     */
    @GetMapping("/crops")
    @RateLimit(limit = 20, windowSize = 60)
    public ResponseEntity<ApiResponse<List<CropResponse>>> getAllCrops() {
        List<CropResponse> crops = cropService.getAllCrops();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Crops retrieved successfully",
                crops,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Retrieves all crops with varieties previously added by the current consultant.
     *
     * Only accessible to users with CONSULTANT role.
     *
     * @return ResponseEntity with ApiResponse containing crops and nested varieties
     */
    @GetMapping("/crops-with-varieties")
    @RateLimit(limit = 20, windowSize = 60)
    public ResponseEntity<ApiResponse<List<ConsultantCropVarietiesResponse>>> getConsultantCropsWithVarieties() {
        List<ConsultantCropVarietiesResponse> cropsWithVarieties = cropVarietyService.getConsultantCropsWithVarieties();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Consultant crops with varieties retrieved successfully",
                cropsWithVarieties,
                HttpStatus.OK.value()
        ));
    }

    @GetMapping("/summary/active-counts")
    public ResponseEntity<ApiResponse<ConsultantActiveSummaryResponse>> getConsultantActiveSummary() {
        ConsultantActiveSummaryResponse summary = cropVarietyService.getConsultantActiveSummary();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Consultant active summary retrieved successfully",
                summary,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Creates a new crop variety for the consultant.
     *
     * Rate limited to 20 requests per 60 seconds.
     * Only accessible to users with CONSULTANT role.
     *
     * @param request the crop variety registration request
     * @return ResponseEntity with ApiResponse containing created crop variety
     */
    @PostMapping("/crop-varieties")
    @RateLimit(limit = 20, windowSize = 60)
    public ResponseEntity<ApiResponse<CropVarietyResponse>> addCropVariety(
            @Valid @RequestBody CropVarietyRegistrationRequest request) {
        CropVarietyResponse response = cropVarietyService.addCropVariety(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Crop variety created successfully",
                        response,
                        HttpStatus.CREATED.value()
                ));
    }

    /**
     * Retrieves active master schedules for a crop variety owned by the current consultant.
     *
     * Only accessible to users with CONSULTANT role.
     *
     * @param cropVarietyId the crop variety ID
     * @return ResponseEntity with ApiResponse containing active master schedules
     */
    @GetMapping("/crop-varieties/{cropVarietyId}/master-schedules")
    @RateLimit(limit = 20, windowSize = 60)
    public ResponseEntity<ApiResponse<List<MasterScheduleTemplateDTO>>> getActiveMasterSchedulesByCropVariety(
            @PathVariable Long cropVarietyId) {
        Long consultantId = getCurrentConsultantProfileId();
        List<MasterScheduleTemplateDTO> schedules =
            masterScheduleService.getActiveTemplatesByConsultantAndCropVariety(consultantId, cropVarietyId);

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Active master schedules retrieved successfully",
            schedules,
            HttpStatus.OK.value()
        ));
    }

    /**
     * Retrieves farmers portfolio (list of all farmers assigned to consultant).
     *
     * Only accessible to users with CONSULTANT role.
     *
     * @return ResponseEntity with ApiResponse containing list of farmers
     */
    @GetMapping("/farmers-portfolio")
    public ResponseEntity<ApiResponse<List<FarmerPortfolioResponse>>> getFarmersPortfolio() {
        List<FarmerPortfolioResponse> portfolio = cropVarietyService.getFarmersPortfolio();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Farmers portfolio retrieved successfully",
                portfolio,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Retrieves detailed information about a specific farmer.
     *
     * Only accessible to users with CONSULTANT role.
     *
     * @param farmerId the farmer user profile ID
     * @return ResponseEntity with ApiResponse containing farmer details
     */
    @GetMapping("/farmers/{farmerId}")
    public ResponseEntity<ApiResponse<FarmerProfileDetailResponse>> getFarmerProfileDetail(
            @PathVariable Long farmerId) {
        FarmerProfileDetailResponse response = cropVarietyService.getFarmerProfileDetail(farmerId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Farmer profile retrieved successfully",
                response,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Assigns a crop variety to a farmer.
     *
     * Rate limited to 20 requests per 60 seconds.
     * Only accessible to users with CONSULTANT role.
     *
     * @param farmerId the farmer user profile ID
     * @param request the crop variety assignment request
     * @return ResponseEntity with ApiResponse containing assignment details
     */
    @PostMapping("/farmers/{farmerId}/crops")
    @RateLimit(limit = 20, windowSize = 60)
    public ResponseEntity<ApiResponse<FarmerCropVarietyResponse>> assignCropVarietyToFarmer(
            @PathVariable Long farmerId,
            @Valid @RequestBody AssignCropVarietyRequest request) {
        FarmerCropVarietyResponse response = cropVarietyService.assignCropVarietyToFarmer(farmerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Crop variety assigned to farmer successfully",
                        response,
                        HttpStatus.CREATED.value()
                ));
    }

    /**
     * Updates a farmer's crop variety assignment.
     *
     * Only accessible to users with CONSULTANT role.
     *
     * @param farmerId the farmer user profile ID
     * @param cropVarietyId the farmer crop variety assignment ID
     * @param request the updated assignment request
     * @return ResponseEntity with ApiResponse containing updated assignment
     */
    @PutMapping("/farmers/{farmerId}/crops/{cropVarietyId}")
    public ResponseEntity<ApiResponse<FarmerCropVarietyResponse>> updateFarmerCropVariety(
            @PathVariable Long farmerId,
            @PathVariable Long cropVarietyId,
            @Valid @RequestBody AssignCropVarietyRequest request) {
        FarmerCropVarietyResponse response = cropVarietyService.updateFarmerCropVariety(farmerId, cropVarietyId, request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Crop variety updated successfully",
                response,
                HttpStatus.OK.value()
        ));
    }


    @GetMapping("/farmer/{farmerId}/schedules/{farmerCropVarietyId}")
    public ResponseEntity<ApiResponse<FarmerScheduleResponse>> getFarmerSchedules(
            @PathVariable @Positive Long farmerId,
            @PathVariable @Positive Long farmerCropVarietyId) {
        UserProfile farmerProfile = userProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found"));
        FarmerScheduleResponse response = sendScheduleService.getFarmerSchedule(farmerProfile,farmerCropVarietyId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Farmer schedules retrieved successfully",
                response,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Creates a new advertisement for the current consultant.
     *
     * Rate limited to 20 requests per 60 seconds.
     * Only accessible to users with CONSULTANT role.
     *
     * Business Logic:
     * 1. Extracts current authenticated consultant from SecurityContext
     * 2. Receives advertisement details from client
     * 3. Creates ConsultantAdvertisement entity linked to consultant
     * 4. Stores URL, title, descriptions, and priority for display to farmers
     * 5. Returns created advertisement information
     *
     * @param request the advertisement request with url, title, descriptions, and priority
     * @return ResponseEntity with ApiResponse containing created advertisement information
     * @throws ResourceNotFoundException if current consultant not found
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    @PostMapping("/advertisements")
    @RateLimit(limit = 20, windowSize = 60)
    public ResponseEntity<ApiResponse<ConsultantAdvertisementResponse>> addAdvertisement(
            @Valid @RequestBody ConsultantAdvertisementRequest request) {
        ConsultantAdvertisementResponse response = advertisementService.addAdvertisement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Advertisement created successfully",
                        response,
                        HttpStatus.CREATED.value()
                ));
    }

    /**
     * Retrieves all advertisements created by the current consultant.
     *
     * Only accessible to users with CONSULTANT role.
     * Read-only operation.
     *
     * Business Logic:
     * 1. Extracts current authenticated consultant from SecurityContext
     * 2. Retrieves all advertisements created by this consultant
     * 3. Returns list of advertisements ordered by priority
     *
     * @return ResponseEntity with ApiResponse containing list of advertisements
     * @throws ResourceNotFoundException if current consultant not found
     */
    @GetMapping("/advertisements")
    public ResponseEntity<ApiResponse<List<ConsultantAdvertisementResponse>>> getAdvertisements() {
        List<ConsultantAdvertisementResponse> advertisements = advertisementService.getConsultantAdvertisements();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisements retrieved successfully",
                advertisements,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Updates an existing advertisement created by the current consultant.
     *
     * Only accessible to users with CONSULTANT role.
     * Consultant can only update advertisements they created.
     *
     * @param advertisementId the ID of the advertisement to update
     * @param request the updated advertisement request
     * @return ResponseEntity with ApiResponse containing updated advertisement information
     * @throws ResourceNotFoundException if advertisement not found
     * @throws AccessDeniedException if consultant doesn't own the advertisement
     */
    @PutMapping("/advertisements/{advertisementId}")
    public ResponseEntity<ApiResponse<ConsultantAdvertisementResponse>> updateAdvertisement(
            @PathVariable Long advertisementId,
            @Valid @RequestBody ConsultantAdvertisementRequest request) {
        ConsultantAdvertisementResponse response = advertisementService.updateAdvertisement(advertisementId, request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisement updated successfully",
                response,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Deletes an advertisement created by the current consultant.
     *
     * Only accessible to users with CONSULTANT role.
     * Consultant can only delete advertisements they created.
     *
     * @param advertisementId the ID of the advertisement to delete
     * @return ResponseEntity with ApiResponse indicating successful deletion
     * @throws ResourceNotFoundException if advertisement not found
     * @throws AccessDeniedException if consultant doesn't own the advertisement
     */
    @DeleteMapping("/advertisements/{advertisementId}")
    public ResponseEntity<ApiResponse<Void>> deleteAdvertisement(
            @PathVariable Long advertisementId) {
        advertisementService.deleteAdvertisement(advertisementId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Advertisement deleted successfully",
                null,
                HttpStatus.OK.value()
        ));
    }
}
