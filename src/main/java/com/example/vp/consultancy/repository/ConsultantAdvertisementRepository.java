package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.ConsultantAdvertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ConsultantAdvertisement entity operations.
 * Provides data access methods for managing consultant advertisements.
 * @author VP Consultancy Team
 * @version 1.0
 */
@Repository
public interface ConsultantAdvertisementRepository extends JpaRepository<ConsultantAdvertisement, Long> {

    /**
     * Retrieves all advertisements created by a specific consultant, ordered by priority (descending).
     *
     * @param consultantId the consultant's user profile ID
     * @return List of ConsultantAdvertisement ordered by priority
     */
    @Query("SELECT ca FROM ConsultantAdvertisement ca WHERE ca.consultant.id = :consultantId ORDER BY ca.priority DESC, ca.createdAt DESC")
    List<ConsultantAdvertisement> findByConsultantIdOrderByPriority(@Param("consultantId") Long consultantId);

    /**
     * Retrieves all advertisements in the system, ordered by priority (descending).
     * Used for displaying advertisements to farmers.
     *
     * @return List of all ConsultantAdvertisement ordered by priority
     */
    @Query("SELECT ca FROM ConsultantAdvertisement ca ORDER BY ca.priority DESC, ca.createdAt DESC")
    List<ConsultantAdvertisement> findAllOrderByPriority();
}
