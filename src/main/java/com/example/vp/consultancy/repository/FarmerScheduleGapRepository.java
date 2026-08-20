package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.FarmerScheduleGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmerScheduleGapRepository extends JpaRepository<FarmerScheduleGap, Long> {

    @Query("SELECT COALESCE(SUM(g.gapDays), 0) FROM FarmerScheduleGap g WHERE g.farmer.id = :farmerId AND g.farmerCropVariety.id = :farmerCropVarietyId")
    Long getTotalGapDays(@Param("farmerId") Long farmerId, @Param("farmerCropVarietyId") Long farmerCropVarietyId);
}

