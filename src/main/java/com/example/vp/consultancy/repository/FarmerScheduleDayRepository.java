package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.FarmerScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerScheduleDayRepository extends JpaRepository<FarmerScheduleDay, Long> {

    @Query("SELECT fsd FROM FarmerScheduleDay fsd " +
           "WHERE fsd.schedule.id = :scheduleId AND fsd.dayNumber = :dayNumber")
    Optional<FarmerScheduleDay> findByScheduleIdAndDayNumber(@Param("scheduleId") Long scheduleId, 
                                                              @Param("dayNumber") Long dayNumber);

    @Query("SELECT fsd FROM FarmerScheduleDay fsd " +
           "WHERE fsd.farmer.id = :farmerId " +
           "AND fsd.farmerCropVariety.id = :farmerCropVarietyId " +
           "AND fsd.dayNumber = :dayNumber")
    Optional<FarmerScheduleDay> findByFarmerIdAndFarmerCropVarietyIdAndDayNumber(
            @Param("farmerId") Long farmerId,
            @Param("farmerCropVarietyId") Long farmerCropVarietyId,
            @Param("dayNumber") Long dayNumber);
}
