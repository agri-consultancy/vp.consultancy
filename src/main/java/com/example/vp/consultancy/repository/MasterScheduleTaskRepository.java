package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.MasterScheduleTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterScheduleTaskRepository extends JpaRepository<MasterScheduleTask, Long> {
    List<MasterScheduleTask> findByScheduleDayId(Long scheduleDayId);
}
