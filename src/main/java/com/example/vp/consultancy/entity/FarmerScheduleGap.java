package com.example.vp.consultancy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "farmer_schedule_gaps", indexes = {
        @Index(name = "idx_farmer_schedule_gap_farmer", columnList = "farmer_id"),
        @Index(name = "idx_farmer_schedule_gap_variety", columnList = "farmer_crop_variety_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerScheduleGap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private UserProfile farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_crop_variety_id", nullable = false)
    private FarmerCropVariety farmerCropVariety;

    @Column(nullable = false)
    private Long gapDays;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

