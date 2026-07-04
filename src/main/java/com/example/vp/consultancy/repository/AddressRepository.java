package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Address Repository - Data access layer for Address entity
 * @author VP Consultancy Team
 * @version 1.0
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
