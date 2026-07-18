package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.dto.ConsultantAdvertisementRequest;
import com.example.vp.consultancy.dto.ConsultantAdvertisementResponse;
import com.example.vp.consultancy.entity.ConsultantAdvertisement;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.ConsultantAdvertisementRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.service.ConsultantAdvertisementService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ConsultantAdvertisementService.
 * Handles all consultant advertisement management operations.
 */
@Service
@Transactional
public class ConsultantAdvertisementServiceImpl implements ConsultantAdvertisementService {

    private final ConsultantAdvertisementRepository advertisementRepository;
    private final UserProfileRepository userProfileRepository;

    public ConsultantAdvertisementServiceImpl(
            ConsultantAdvertisementRepository advertisementRepository,
            UserProfileRepository userProfileRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public ConsultantAdvertisementResponse addAdvertisement(ConsultantAdvertisementRequest request) {
        Assert.notNull(request, "Advertisement request cannot be null");
        Assert.hasText(request.getUrl(), "URL is required");
        Assert.hasText(request.getTitle(), "Title is required");
        Assert.hasText(request.getDescriptions(), "Description is required");
        Assert.notNull(request.getPriority(), "Priority is required");
        Assert.hasText(request.getType(), "Type is required");

        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));

        // Create advertisement
        ConsultantAdvertisement advertisement = new ConsultantAdvertisement();
        advertisement.setConsultant(consultant);
        advertisement.setUrl(request.getUrl());
        advertisement.setTitle(request.getTitle());
        advertisement.setDescriptions(request.getDescriptions());
        advertisement.setPriority(request.getPriority());
        advertisement.setType(request.getType().trim().toLowerCase());

        ConsultantAdvertisement savedAdvertisement = advertisementRepository.save(advertisement);

        return convertToResponse(savedAdvertisement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultantAdvertisementResponse> getConsultantAdvertisements() {
        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));

        return advertisementRepository.findByConsultantIdOrderByPriority(consultant.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultantAdvertisementResponse> getAllAdvertisements() {
        return advertisementRepository.findAllOrderByPriority()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultantAdvertisementResponse updateAdvertisement(Long advertisementId, ConsultantAdvertisementRequest request) {
        Assert.notNull(advertisementId, "Advertisement ID cannot be null");
        Assert.notNull(request, "Advertisement request cannot be null");

        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));

        // Get advertisement
        ConsultantAdvertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found with ID: " + advertisementId));

        // Check authorization
        if (!advertisement.getConsultant().getId().equals(consultant.getId())) {
            throw new AccessDeniedException("You are not authorized to update this advertisement");
        }

        // Update fields
        advertisement.setUrl(request.getUrl());
        advertisement.setTitle(request.getTitle());
        advertisement.setDescriptions(request.getDescriptions());
        advertisement.setPriority(request.getPriority());
        if (StringUtils.hasText(request.getType())) {
            advertisement.setType(request.getType().trim().toLowerCase());
        }

        ConsultantAdvertisement updatedAdvertisement = advertisementRepository.save(advertisement);

        return convertToResponse(updatedAdvertisement);
    }

    @Override
    public void deleteAdvertisement(Long advertisementId) {
        Assert.notNull(advertisementId, "Advertisement ID cannot be null");

        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));

        // Get advertisement
        ConsultantAdvertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found with ID: " + advertisementId));

        // Check authorization
        if (!advertisement.getConsultant().getId().equals(consultant.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this advertisement");
        }

        advertisementRepository.delete(advertisement);
    }

    private ConsultantAdvertisementResponse convertToResponse(ConsultantAdvertisement advertisement) {
        String consultantName = "N/A";
        if (advertisement.getConsultant() != null) {
            consultantName = advertisement.getConsultant().getFirstName() + " " + 
                           advertisement.getConsultant().getLastName();
        }

        return ConsultantAdvertisementResponse.builder()
                .id(advertisement.getId())
                .url(advertisement.getUrl())
                .title(advertisement.getTitle())
                .descriptions(advertisement.getDescriptions())
                .priority(advertisement.getPriority())
                .type(advertisement.getType())
                .consultantId(advertisement.getConsultant() != null ? advertisement.getConsultant().getId() : null)
                .consultantName(consultantName)
                .createdAt(advertisement.getCreatedAt() != null ? advertisement.getCreatedAt().toString() : null)
                .updatedAt(advertisement.getUpdatedAt() != null ? advertisement.getUpdatedAt().toString() : null)
                .build();
    }
}
