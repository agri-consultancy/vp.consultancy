package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.dto.CropRegistrationRequest;
import com.example.vp.consultancy.dto.CropResponse;
import com.example.vp.consultancy.entity.Crop;
import com.example.vp.consultancy.exception.DuplicateResourceException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.CropRepository;
import com.example.vp.consultancy.service.CropService;
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

    private final CropRepository cropRepository;

    public CropServiceImpl(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    public CropResponse addCrop(CropRegistrationRequest request) {
        Assert.notNull(request, "Crop registration request cannot be null");
        Assert.hasText(request.getName(), "Crop name is required");

        String cropName = request.getName().trim();

        if (cropRepository.existsByName(cropName)) {
            throw new DuplicateResourceException("Crop '" + cropName + "' already exists");
        }

        Crop crop = new Crop();
        crop.setName(cropName);
        crop.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Crop savedCrop = cropRepository.save(crop);

        return convertToCropResponse(savedCrop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> getAllCrops() {
        return cropRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::convertToCropResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CropResponse getCropById(Long cropId) {
        Assert.notNull(cropId, "Crop ID cannot be null");

        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with ID: " + cropId));

        return convertToCropResponse(crop);
    }

    private CropResponse convertToCropResponse(Crop crop) {
        return CropResponse.builder()
                .id(crop.getId())
                .name(crop.getName())
                .description(crop.getDescription())
                .build();
    }
}

