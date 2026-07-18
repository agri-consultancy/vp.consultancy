package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.CropRegistrationRequest;
import com.example.vp.consultancy.dto.CropResponse;

import java.util.List;

/**
 * Service interface for crop management operations.
 *
 * This service provides methods for:
 * - Adding new crops (admin only)
 * - Retrieving all crops
 * - Retrieving crop by ID
 *
 * @author VP Consultancy Team
 * @version 1.0
 */
public interface CropService {

    /**
     * Registers a new crop to the system.
     *
     * @param request the crop registration request
     * @return CropResponse containing created crop information
     * @throws com.example.vp.consultancy.exception.DuplicateResourceException if crop name already exists
     */
    CropResponse addCrop(CropRegistrationRequest request);

    /**
     * Retrieves all crops sorted by name.
     *
     * @return List of CropResponse DTOs for all crops
     */
    List<CropResponse> getAllCrops();

    /**
     * Retrieves a crop by its ID.
     *
     * @param cropId the ID of the crop to retrieve
     * @return CropResponse containing crop information
     * @throws com.example.vp.consultancy.exception.ResourceNotFoundException if crop doesn't exist
     */
    CropResponse getCropById(Long cropId);
}

