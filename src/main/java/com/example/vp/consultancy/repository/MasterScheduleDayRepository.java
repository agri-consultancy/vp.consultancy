package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.MasterScheduleDay;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterScheduleDayRepository extends JpaRepository<MasterScheduleDay, Long> {
    List<MasterScheduleDay> findByTemplateId(Long templateId);
    boolean existsByTemplateIdAndDayNumber(Long templateId, Long dayNumber);

    @EntityGraph(attributePaths = {"tasks"})
    List<MasterScheduleDay> findByTemplateIdAndDayNumberBetweenOrderByDayNumberAsc(Long templateId, Long startDay, Long endDay);
}
