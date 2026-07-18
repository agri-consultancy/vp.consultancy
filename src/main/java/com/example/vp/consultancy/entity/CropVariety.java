package com.example.vp.consultancy.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * CropVariety Entity - Represents a specific variety of a crop
 * Created by consultants to define supported varieties with agricultural metrics
 * @author VP Consultancy Team
 * @version 1.0
 */
@Entity
@Table(name = "crop_varieties", indexes = {
    @Index(name = "idx_crop_id", columnList = "crop_id"),
    @Index(name = "idx_consultant_id", columnList = "consultant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropVariety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id")
    private UserProfile consultant;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String climate;

    @Column(length = 255)
    private String yieldPotential;

    @Column
    private Long cycleDurationDays;

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

