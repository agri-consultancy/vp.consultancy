package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.FarmerCropVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FarmerCropVariety Repository - Data access layer for FarmerCropVariety entity
 * @author VP Consultancy Team
 * @version 1.0
 */
@Repository
public interface FarmerCropVarietyRepository extends JpaRepository<FarmerCropVariety, Long> {

    /**
     * Find all crop varieties assigned to a farmer
     * @param farmerId the farmer user profile ID
     * @return List of farmer crop varieties
     */
    List<FarmerCropVariety> findByFarmerId(Long farmerId);

    /**
     * Find a specific farmer crop variety assignment
     * @param farmerId the farmer user profile ID
     * @param cropVarietyId the crop variety ID
     * @return Optional containing the assignment if found
     */
    Optional<FarmerCropVariety> findByFarmerIdAndCropVarietyId(Long farmerId, Long cropVarietyId);

    /**
     * Find all crop varieties by status for a farmer
     * @param farmerId the farmer user profile ID
     * @param status the status (Preparing, Active, Harvesting, Completed)
     * @return List of farmer crop varieties with given status
     */
    List<FarmerCropVariety> findByFarmerIdAndStatus(Long farmerId, String status);

    @Query("SELECT COUNT(fcv) FROM FarmerCropVariety fcv " +
            "WHERE fcv.farmer.consultant.id = :consultantUserId " +
            "AND UPPER(COALESCE(fcv.status, '')) = 'ACTIVE'")
    long countActiveByConsultantUserId(@Param("consultantUserId") Long consultantUserId);
}

