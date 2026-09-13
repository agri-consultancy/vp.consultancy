package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.FarmerScheduleGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmerScheduleGapRepository extends JpaRepository<FarmerScheduleGap, Long> {

    @Query("SELECT COALESCE(SUM(g.gapDays), 0) FROM FarmerScheduleGap g WHERE g.farmer.id = :farmerId AND g.farmerCropVariety.id = :farmerCropVarietyId")
    Long getTotalGapDays(@Param("farmerId") Long farmerId, @Param("farmerCropVarietyId") Long farmerCropVarietyId);

    @Query("SELECT COALESCE(SUM(g.gapDays), 0) FROM FarmerScheduleGap g WHERE g.farmerCropVariety.id = :farmerCropVarietyId")
    Long getTotalGapDaysForVariety(@Param("farmerCropVarietyId") Long farmerCropVarietyId);

    /**
     * Find all gaps for a specific farmer crop variety.
     * Used for deletion when unassigning a variety.
     * @param farmerCropVarietyId the farmer crop variety ID
     * @return List of all gaps for this farmer crop variety
     */
    List<FarmerScheduleGap> findByFarmerCropVarietyId(Long farmerCropVarietyId);
}

