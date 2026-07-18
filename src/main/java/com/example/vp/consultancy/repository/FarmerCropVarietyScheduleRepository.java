package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.FarmerCropVarietySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerCropVarietyScheduleRepository extends JpaRepository<FarmerCropVarietySchedule, Long> {

    Optional<FarmerCropVarietySchedule> findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(
            Long farmerId, Long farmerCropVarietyId);

    List<FarmerCropVarietySchedule> findByFarmerCropVarietyIdOrderByIdAsc(Long farmerCropVarietyId);
}
