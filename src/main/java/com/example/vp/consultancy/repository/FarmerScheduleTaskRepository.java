package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.FarmerScheduleTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmerScheduleTaskRepository extends JpaRepository<FarmerScheduleTask, Long> {
}
