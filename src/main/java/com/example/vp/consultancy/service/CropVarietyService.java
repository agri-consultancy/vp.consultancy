package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.AssignCropVarietyRequest;
import com.example.vp.consultancy.dto.ConsultantCropVarietiesResponse;
import com.example.vp.consultancy.dto.ConsultantActiveSummaryResponse;
import com.example.vp.consultancy.dto.CropVarietyRegistrationRequest;
import com.example.vp.consultancy.dto.CropVarietyResponse;
import com.example.vp.consultancy.dto.FarmerCropVarietyResponse;
import com.example.vp.consultancy.dto.FarmerPortfolioResponse;
import com.example.vp.consultancy.dto.FarmerProfileDetailResponse;
import com.example.vp.consultancy.dto.FarmerProfileResponse;

import java.util.List;

/**
 * Service interface for crop variety management operations.
 * Handles creation of crop varieties and assignment to farmers.
 * @author VP Consultancy Team
 * @version 1.0
 */
public interface CropVarietyService {

    /**
     * Creates a new crop variety for the current consultant.
     * @param request crop variety registration request
     * @return CropVarietyResponse containing created variety details
     */
    CropVarietyResponse addCropVariety(CropVarietyRegistrationRequest request);

    /**
     * Updates an existing crop variety created by the current consultant.
     * Only the consultant who created the variety can edit it.
     * @param cropVarietyId the crop variety ID to update
     * @param request crop variety registration request with updated details
     * @return CropVarietyResponse containing updated variety details
     * @throws ResourceNotFoundException if variety not found or not owned by current consultant
     */
    CropVarietyResponse updateCropVariety(Long cropVarietyId, CropVarietyRegistrationRequest request);

    /**
     * Assigns a crop variety to a farmer.
     * @param farmerId the farmer user profile ID
     * @param request crop variety assignment request
     * @return FarmerCropVarietyResponse with assignment details
     */
    FarmerCropVarietyResponse assignCropVarietyToFarmer(Long farmerId, AssignCropVarietyRequest request);

    /**
     * Retrieves all farmers assigned to the current consultant.
     * @return List of FarmerPortfolioResponse
     */
    List<FarmerPortfolioResponse> getFarmersPortfolio();

    /**
     * Retrieves detailed information about a specific farmer.
     * @param farmerId the farmer user profile ID
     * @return FarmerProfileDetailResponse with complete farmer details
     */
    FarmerProfileDetailResponse getFarmerProfileDetail(Long farmerId);

    /**
     * Retrieves all crop varieties assigned to a specific farmer.
     * @param farmerId the farmer user profile ID
     * @return List of FarmerCropVarietyResponse
     */
    List<FarmerCropVarietyResponse> getFarmerCrops(Long farmerId);

    /**
     * Retrieves all crop varieties assigned to the currently authenticated farmer.
     * @return List of FarmerCropVarietyResponse
     */
    List<FarmerCropVarietyResponse> getCurrentFarmerCrops();

    /**
     * Updates a farmer's crop variety assignment.
     * @param farmerId the farmer user profile ID
     * @param cropVarietyAssignmentId the farmer crop variety assignment ID
     * @param request updated crop variety assignment request
     * @return FarmerCropVarietyResponse with updated details
     */
    FarmerCropVarietyResponse updateFarmerCropVariety(Long farmerId, Long cropVarietyAssignmentId, AssignCropVarietyRequest request);

    /**
     * Retrieves all crops and their varieties created by the current consultant.
     * @return List grouped by crop with its varieties
     */
    List<ConsultantCropVarietiesResponse> getConsultantCropsWithVarieties();

    ConsultantActiveSummaryResponse getConsultantActiveSummary();

    FarmerProfileResponse getCurrentFarmerProfile();

    /**
     * Unassigns a crop variety from a farmer.
     * Deletes all associated schedules and schedule days/tasks/gaps.
     *
     * Business Logic:
     * 1. Verify farmer exists
     * 2. Verify FarmerCropVariety assignment exists
     * 3. Delete all FarmerCropVarietySchedule records for this assignment
     * 4. Delete all FarmerScheduleDay records for this assignment
     * 5. Delete all FarmerScheduleTask records for this assignment
     * 6. Delete all FarmerScheduleGap records for this assignment
     * 7. Delete the FarmerCropVariety record itself
     * 8. Clear relevant caches
     *
     * @param farmerId the farmer user profile ID
     * @param farmerCropVarietyId the farmer crop variety assignment ID to unassign
     * @throws ResourceNotFoundException if farmer not found or assignment not found
     */
    void unassignCropVarietyFromFarmer(Long farmerId, Long farmerCropVarietyId);

    /**
     * Deletes a farmer profile and all associated data.
     *
     * Business Logic:
     * 1. Verify consultant owns this farmer
     * 2. Delete all FarmerCropVarietySchedule records
     * 3. Delete all FarmerScheduleDay records (cascaded)
     * 4. Delete all FarmerScheduleTask records (cascaded)
     * 5. Delete all FarmerScheduleGap records
     * 6. Delete all FarmerCropVariety assignments
     * 7. Delete the UserProfile
     * 8. Delete the User entity
     * 9. Delete the Address if associated
     * 10. Clear relevant caches
     *
     * @param farmerId the farmer user profile ID
     * @throws ResourceNotFoundException if farmer not found
     * @throws AccessDeniedException if farmer doesn't belong to current consultant
     */
    void deleteFarmerProfile(Long farmerId);

    /**
     * Deletes a crop variety created by the consultant.
     *
     * Business Logic:
     * 1. Verify consultant owns this crop variety
     * 2. Check if crop variety is assigned to any farmers
     * 3. If assigned, return error response
     * 4. Delete all MasterScheduleTemplate records for this crop variety
     * 5. Delete the CropVariety record itself
     * 6. Clear relevant caches
     *
     * @param cropVarietyId the crop variety ID
     * @throws ResourceNotFoundException if crop variety not found
     * @throws AccessDeniedException if crop variety doesn't belong to current consultant
     */
    void deleteCropVariety(Long cropVarietyId);
}
