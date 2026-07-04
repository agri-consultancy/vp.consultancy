package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserProfile Repository - Data access layer for UserProfile entity
 * @author VP Consultancy Team
 * @version 1.0
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Find user profile by user ID
     * @param userId User ID
     * @return Optional containing UserProfile if found
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Find user profile by email
     * @param email Email address
     * @return Optional containing UserProfile if found
     */
    Optional<UserProfile> findByEmail(String email);

    /**
     * Check if profile exists with given email
     * @param email Email address
     * @return true if profile exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all user profiles for a specific consultant
     * @param consultantId Consultant user ID
     * @return List of UserProfile entities for farmers under this consultant
     */
    List<UserProfile> findByConsultantId(Long consultantId);
}
