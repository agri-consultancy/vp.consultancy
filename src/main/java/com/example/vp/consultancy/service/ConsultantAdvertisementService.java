package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.ConsultantAdvertisementRequest;
import com.example.vp.consultancy.dto.ConsultantAdvertisementResponse;

import java.util.List;

/**
 * Service interface for Consultant Advertisement management operations.
 *
 * This service provides methods for:
 * - Creating new advertisements
 * - Retrieving advertisements (consultant's own or all)
 * - Updating advertisements
 * - Deleting advertisements
 *
 * @author VP Consultancy Team
 * @version 1.0
 */
public interface ConsultantAdvertisementService {

    /**
     * Creates a new advertisement for the current authenticated consultant.
     *
     * @param request the advertisement request containing url, title, descriptions, and priority
     * @return ConsultantAdvertisementResponse containing created advertisement information
     * @throws com.example.vp.consultancy.exception.ResourceNotFoundException if consultant profile not found
     */
    ConsultantAdvertisementResponse addAdvertisement(ConsultantAdvertisementRequest request);

    /**
     * Retrieves all advertisements created by the current authenticated consultant.
     *
     * @return List of ConsultantAdvertisementResponse ordered by priority (descending)
     * @throws com.example.vp.consultancy.exception.ResourceNotFoundException if consultant profile not found
     */
    List<ConsultantAdvertisementResponse> getConsultantAdvertisements();

    /**
     * Retrieves all advertisements in the system for display to farmers.
     * Ordered by priority descending to show most important advertisements first.
     *
     * @return List of ConsultantAdvertisementResponse from all consultants ordered by priority
     */
    List<ConsultantAdvertisementResponse> getAllAdvertisements();

    /**
     * Updates an existing advertisement created by the current consultant.
     *
     * @param advertisementId the ID of the advertisement to update
     * @param request the updated advertisement request
     * @return ConsultantAdvertisementResponse containing updated advertisement information
     * @throws com.example.vp.consultancy.exception.ResourceNotFoundException if advertisement not found
     * @throws org.springframework.security.access.AccessDeniedException if consultant doesn't own the advertisement
     */
    ConsultantAdvertisementResponse updateAdvertisement(Long advertisementId, ConsultantAdvertisementRequest request);

    /**
     * Deletes an advertisement created by the current consultant.
     *
     * @param advertisementId the ID of the advertisement to delete
     * @throws com.example.vp.consultancy.exception.ResourceNotFoundException if advertisement not found
     * @throws org.springframework.security.access.AccessDeniedException if consultant doesn't own the advertisement
     */
    void deleteAdvertisement(Long advertisementId);
}
