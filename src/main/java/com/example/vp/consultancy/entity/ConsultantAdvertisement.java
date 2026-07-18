package com.example.vp.consultancy.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ConsultantAdvertisement Entity - Advertisements created by consultants to show their farmers
 * Maintains consultant-specific promotional content with priority-based ordering
 * @author VP Consultancy Team
 * @version 1.0
 */
@Entity
@Table(name = "consultant_advertisement", indexes = {
    @Index(name = "idx_consultant_id", columnList = "consultant_id"),
    @Index(name = "idx_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantAdvertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultant_id", nullable = false)
    private UserProfile consultant;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 512)
    private String descriptions;

    @Column(nullable = false)
    private Long priority;

    @Column(length = 20)
    private String type;

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
