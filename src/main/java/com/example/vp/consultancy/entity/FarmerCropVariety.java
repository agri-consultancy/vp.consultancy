package com.example.vp.consultancy.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * FarmerCropVariety Entity - Tracks crop varieties assigned to farmers
 * Represents the cultivation of a specific crop variety by a farmer
 * @author VP Consultancy Team
 * @version 1.0
 */
@Entity
@Table(name = "farmer_crop_varieties", indexes = {
    @Index(name = "idx_farmer_id", columnList = "farmer_id"),
    @Index(name = "idx_crop_variety_id", columnList = "crop_variety_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerCropVariety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private UserProfile farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_variety_id", nullable = false)
    private CropVariety cropVariety;

    @Column
    private Double totalLand;

    @Column
    private Double plantToPlantSpacing;

    @Column
    private Double rowToRowSpacing;

    @Column
    private Integer totalPlants;

    @Column
    private LocalDate sowingDate;

    @Column
    private LocalDate expectedHarvestDate;

    @Column(length = 50)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

