package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crop Repository - Data access layer for Crop entity
 * @author VP Consultancy Team
 * @version 1.0
 */
@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {

    /**
     * Find crop by name
     * @param name Crop name
     * @return Optional containing Crop if found
     */
    Optional<Crop> findByName(String name);

    /**
     * Check if crop exists with given name
     * @param name Crop name
     * @return true if crop exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Find all crops sorted by name
     * @return List of all crops
     */
    List<Crop> findAllByOrderByNameAsc();
}

