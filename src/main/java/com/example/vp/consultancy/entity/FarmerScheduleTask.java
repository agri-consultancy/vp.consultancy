package com.example.vp.consultancy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "farmer_schedule_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerScheduleTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_day_id", nullable = false)
    private FarmerScheduleDay scheduleDay;

    @Column(nullable = false, length = 255)
    private String fertilizerName;

    @Column(nullable = false, length = 255)
    private String quantity;

    @Column(length = 50)
    private String proportion;

    @Column
    private Long priority;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 255)
    private String taskType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
