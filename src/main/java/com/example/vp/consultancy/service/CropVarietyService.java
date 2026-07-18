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
}

