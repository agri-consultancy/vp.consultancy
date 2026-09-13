package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.MasterScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterScheduleTemplateRepository extends JpaRepository<MasterScheduleTemplate, Long> {
    List<MasterScheduleTemplate> findByConsultantId(Long consultantId);
    List<MasterScheduleTemplate> findByConsultantIdAndCropVarietyIdAndStatusOrderByVersionDesc(
        Long consultantId, Long cropVarietyId, String status);
    boolean existsByConsultantIdAndCropVarietyIdAndVersion(Long consultantId, Long cropVarietyId, Long version);
    MasterScheduleTemplate findByConsultantIdAndCropVarietyIdAndVersion(Long consultantId, Long cropVarietyId, Long version);

    /**
     * Find all master schedule templates for a crop variety
     * @param cropVarietyId the crop variety ID
     * @return List of all master schedule templates for this crop variety
     */
    List<MasterScheduleTemplate> findByCropVarietyId(Long cropVarietyId);
}
