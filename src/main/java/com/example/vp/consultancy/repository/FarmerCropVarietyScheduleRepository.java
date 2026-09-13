package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.FarmerCropVarietySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerCropVarietyScheduleRepository extends JpaRepository<FarmerCropVarietySchedule, Long> {

    Optional<FarmerCropVarietySchedule> findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(
            Long farmerId, Long farmerCropVarietyId);

    List<FarmerCropVarietySchedule> findByFarmerCropVarietyIdOrderByIdAsc(Long farmerCropVarietyId);

    @Query("SELECT MAX(s.lastSentDay) FROM FarmerCropVarietySchedule s WHERE s.farmerCropVariety.id = :id")
    Long getLastSentDayByFarmerCropVarietyId(Long id);

    /**
     * Find all schedules for a specific farmer crop variety.
     * Used for deletion when unassigning a variety.
     * @param farmerCropVarietyId the farmer crop variety ID
     * @return List of all schedules for this farmer crop variety
     */
    List<FarmerCropVarietySchedule> findAllByFarmerCropVarietyId(Long farmerCropVarietyId);

    /**
     * Find all schedules for a specific farmer.
     * Used for deletion when deleting farmer profile.
     * @param farmerId the farmer user profile ID
     * @return List of all schedules for this farmer
     */
    List<FarmerCropVarietySchedule> findByFarmerId(Long farmerId);
}
