package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.config.JwtAuthenticationFilter;
import com.example.vp.consultancy.dto.ConsultantAdvertisementRequest;
import com.example.vp.consultancy.dto.ConsultantAdvertisementResponse;
import com.example.vp.consultancy.entity.ConsultantAdvertisement;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.ConsultantAdvertisementRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.service.ConsultantAdvertisementService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.slf4j.LoggerFactory;
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

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConsultantAdvertisementServiceImpl.class);
    private final ConsultantAdvertisementRepository advertisementRepository;
    private final UserProfileRepository userProfileRepository;

    public ConsultantAdvertisementServiceImpl(
            ConsultantAdvertisementRepository advertisementRepository,
            UserProfileRepository userProfileRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "consultantAdvertisements", allEntries = true),
            @CacheEvict(cacheNames = "allConsultantAdvertisements", allEntries = true)
    })
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
        logger.info("Adding advertisement for consultant with mobile: {}", consultantMobile);
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));
        logger.info("Consultant profile found: {} {}", consultant.getFirstName(), consultant.getLastName());
        // Create advertisement
        ConsultantAdvertisement advertisement = new ConsultantAdvertisement();
        advertisement.setConsultant(consultant);
        advertisement.setUrl(request.getUrl());
        advertisement.setTitle(request.getTitle());
        advertisement.setDescriptions(request.getDescriptions());
        advertisement.setPriority(request.getPriority());
        advertisement.setType(request.getType().trim().toLowerCase());

        ConsultantAdvertisement savedAdvertisement = advertisementRepository.save(advertisement);
        logger.info("Advertisement saved with ID: {}", savedAdvertisement.getId());
        return convertToResponse(savedAdvertisement);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "consultantAdvertisements",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public List<ConsultantAdvertisementResponse> getConsultantAdvertisements() {
        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        logger.info("Fetching advertisements for consultant with mobile: {}", consultantMobile);
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));
        logger.debug("Consultant profile found: {} {}", consultant.getFirstName(), consultant.getLastName());
        return advertisementRepository.findByConsultantIdOrderByPriority(consultant.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "allConsultantAdvertisements")
    public List<ConsultantAdvertisementResponse> getAllAdvertisements() {
        logger.info("Fetching all advertisements");
        return advertisementRepository.findAllOrderByPriority()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "consultantAdvertisements", allEntries = true),
            @CacheEvict(cacheNames = "allConsultantAdvertisements", allEntries = true)
    })
    public ConsultantAdvertisementResponse updateAdvertisement(Long advertisementId, ConsultantAdvertisementRequest request) {
        Assert.notNull(advertisementId, "Advertisement ID cannot be null");
        Assert.notNull(request, "Advertisement request cannot be null");

        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        logger.info("Updating advertisement with ID: {} for consultant with mobile: {}", advertisementId, consultantMobile);
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));
        logger.debug("Consultant profile found : {} {}", consultant.getFirstName(), consultant.getLastName());
        // Get advertisement
        ConsultantAdvertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found with ID: " + advertisementId));
         logger.info("Advertisement found with ID: {} for consultant ID: {}", advertisementId, advertisement.getConsultant().getId());
        // Check authorization
        if (!advertisement.getConsultant().getId().equals(consultant.getId())) {
            logger.error("Unauthorized update attempt by consultant with ID: {} on advertisement with ID: {}", consultant.getId(), advertisementId);
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
        logger.info("Advertisement updated with ID: {} for consultant ID: {}", updatedAdvertisement.getId(), updatedAdvertisement.getConsultant().getId());
        return convertToResponse(updatedAdvertisement);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "consultantAdvertisements", allEntries = true),
            @CacheEvict(cacheNames = "allConsultantAdvertisements", allEntries = true)
    })
    public void deleteAdvertisement(Long advertisementId) {
        Assert.notNull(advertisementId, "Advertisement ID cannot be null");

        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        logger.info("Deleting advertisement with ID: {} for consultant with mobile: {}", advertisementId, consultantMobile);
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant profile not found"));
        logger.info("Consultant profile found : {} {}", consultant.getFirstName(), consultant.getLastName());
        // Get advertisement
        ConsultantAdvertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found with ID: " + advertisementId));

        // Check authorization
        if (!advertisement.getConsultant().getId().equals(consultant.getId())) {
            logger.error("Unauthorized delete attempt by consultant with ID: {} on advertisement with ID: {}", consultant.getId(), advertisementId);
            throw new AccessDeniedException("You are not authorized to delete this advertisement");
        }

        advertisementRepository.delete(advertisement);
        logger.info("Advertisement deleted with ID: {} for consultant ID: {}", advertisementId, consultant.getId());
    }

    private ConsultantAdvertisementResponse convertToResponse(ConsultantAdvertisement advertisement) {
        String consultantName = "N/A";
        logger.info("Converting advertisement with ID: {} to response DTO", advertisement.getId());
        if (advertisement.getConsultant() != null) {
            consultantName = advertisement.getConsultant().getFirstName() + " " + 
                           advertisement.getConsultant().getLastName();
        }
        logger.info("Converting advertisement with ID: {} to response DTO for consultant: {}", advertisement.getId(), consultantName);
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
