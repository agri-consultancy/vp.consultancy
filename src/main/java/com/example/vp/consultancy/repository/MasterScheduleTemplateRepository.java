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
}
