package com.example.vp.consultancy.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User Entity - Core user table for authentication and authorization
 * Contains user credentials and role information
 * @author VP Consultancy Team
 * @version 1.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_mobile", columnList = "mobile"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 13)
    private String mobile;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "is_mobile_verified")
    private Boolean isMobileVerified;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified;

    @Column(name = "failed_login_attempts")
    private Long failedLoginAttempts;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        failedLoginAttempts = 0L;
        isMobileVerified = false;
        isEmailVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
