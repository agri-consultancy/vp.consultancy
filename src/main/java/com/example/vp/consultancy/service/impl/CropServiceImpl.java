package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.config.JwtAuthenticationFilter;
import com.example.vp.consultancy.dto.CropRegistrationRequest;
import com.example.vp.consultancy.dto.CropResponse;
import com.example.vp.consultancy.entity.Crop;
import com.example.vp.consultancy.exception.DuplicateResourceException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.CropRepository;
import com.example.vp.consultancy.service.CropService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CropService.
 * Handles all crop management operations.
 */
@Service
@Transactional
public class CropServiceImpl implements CropService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CropServiceImpl.class);
    private final CropRepository cropRepository;

    public CropServiceImpl(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "cropsAll", allEntries = true),
            @CacheEvict(cacheNames = "consultantCropsWithVarieties", allEntries = true)
    })
    public CropResponse addCrop(CropRegistrationRequest request) {
        Assert.notNull(request, "Crop registration request cannot be null");
        Assert.hasText(request.getName(), "Crop name is required");

        String cropName = request.getName().trim();
        logger.info("Attempting to add new crop: {}", cropName);
        if (cropRepository.existsByName(cropName)) {
            logger.error("Crop '{}' already exists, cannot add duplicate", cropName);
            throw new DuplicateResourceException("Crop '" + cropName + "' already exists");
        }

        Crop crop = new Crop();
        crop.setName(cropName);
        crop.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Crop savedCrop = cropRepository.save(crop);
        logger.info("Successfully added new crop: {}", savedCrop.getName());
        return convertToCropResponse(savedCrop);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cropsAll")
    public List<CropResponse> getAllCrops() {
        logger.info("Fetching all crops from the database");
        return cropRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::convertToCropResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cropById", key = "#cropId")
    public CropResponse getCropById(Long cropId) {
        Assert.notNull(cropId, "Crop ID cannot be null");
        logger.info("Fetching crop with ID: {}", cropId);
        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with ID: " + cropId));

        logger.info("Found crop with ID: {}", cropId);
        return convertToCropResponse(crop);
    }

    private CropResponse convertToCropResponse(Crop crop) {
        logger.info("Converting Crop entity to CropResponse DTO for crop ID: {}", crop.getId());
        return CropResponse.builder()
                .id(crop.getId())
                .name(crop.getName())
                .description(crop.getDescription())
                .build();
    }
}

