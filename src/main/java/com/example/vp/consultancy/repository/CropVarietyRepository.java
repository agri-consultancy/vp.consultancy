package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.CropVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CropVariety Repository - Data access layer for CropVariety entity
 * @author VP Consultancy Team
 * @version 1.0
 */
@Repository
public interface CropVarietyRepository extends JpaRepository<CropVariety, Long> {

    /**
     * Find all crop varieties for a specific crop
     * @param cropId the crop ID
     * @return List of crop varieties
     */
    List<CropVariety> findByCropId(Long cropId);

    /**
     * Find all crop varieties created by a consultant
     * @param consultantId the consultant user profile ID
     * @return List of crop varieties
     */
    List<CropVariety> findByConsultantId(Long consultantId);

    /**
     * Find a crop variety by ID for a specific consultant.
     * @param id the crop variety ID
     * @param consultantId the consultant user profile ID
     * @return Optional containing crop variety if found
     */
    Optional<CropVariety> findByIdAndConsultantId(Long id, Long consultantId);

    /**
     * Find a specific crop variety by ID
     * @param id the crop variety ID
     * @return Optional containing crop variety if found
     */
    Optional<CropVariety> findById(Long id);
}

