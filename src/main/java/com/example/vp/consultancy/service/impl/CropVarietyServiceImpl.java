package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.config.JwtAuthenticationFilter;
import com.example.vp.consultancy.dto.AssignCropVarietyRequest;
import com.example.vp.consultancy.dto.ConsultantCropVarietiesResponse;
import com.example.vp.consultancy.dto.ConsultantActiveSummaryResponse;
import com.example.vp.consultancy.dto.CropVarietyRegistrationRequest;
import com.example.vp.consultancy.dto.CropVarietyResponse;
import com.example.vp.consultancy.dto.FarmerCropVarietyResponse;
import com.example.vp.consultancy.dto.FarmerPortfolioResponse;
import com.example.vp.consultancy.dto.FarmerProfileDetailResponse;
import com.example.vp.consultancy.dto.FarmerProfileResponse;
import com.example.vp.consultancy.entity.Crop;
import com.example.vp.consultancy.entity.CropVariety;
import com.example.vp.consultancy.entity.FarmerCropVariety;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.entity.UserRole;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.*;
import com.example.vp.consultancy.service.CropVarietyService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of CropVarietyService.
 * Handles crop variety creation and farmer crop assignment.
 */
@Service
@Transactional
public class CropVarietyServiceImpl implements CropVarietyService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CropVarietyServiceImpl.class);
    private final CropVarietyRepository cropVarietyRepository;
    private final FarmerCropVarietyRepository farmerCropVarietyRepository;
    private final FarmerCropVarietyScheduleRepository farmerCropVarietyScheduleRepository;
    private final CropRepository cropRepository;
    private final UserProfileRepository userProfileRepository;

    public CropVarietyServiceImpl(CropVarietyRepository cropVarietyRepository,
                                  FarmerCropVarietyRepository farmerCropVarietyRepository, FarmerCropVarietyScheduleRepository farmerCropVarietyScheduleRepository,
                                  CropRepository cropRepository,
                                  UserProfileRepository userProfileRepository) {
        this.cropVarietyRepository = cropVarietyRepository;
        this.farmerCropVarietyRepository = farmerCropVarietyRepository;
        this.farmerCropVarietyScheduleRepository = farmerCropVarietyScheduleRepository;
        this.cropRepository = cropRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "consultantCropsWithVarieties", allEntries = true),
            @CacheEvict(cacheNames = "activeTemplatesByConsultantAndVariety", allEntries = true)
    })
    public CropVarietyResponse addCropVariety(CropVarietyRegistrationRequest request) {
        Assert.notNull(request, "Crop variety registration request cannot be null");
        Assert.notNull(request.getCropId(), "Crop ID is required");
        Assert.hasText(request.getName(), "Variety name is required");

        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));
        logger.info("Consultant {} is adding a new crop variety: {}", consultant.getUser().getId(), request.getName());

        // Get crop
        Crop crop = cropRepository.findById(request.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with ID: " + request.getCropId()));
        logger.info("Found crop {} for variety addition", crop.getName());

        // Create crop variety
        CropVariety cropVariety = new CropVariety();
        cropVariety.setCrop(crop);
        cropVariety.setConsultant(consultant);
        cropVariety.setName(request.getName());
        cropVariety.setDescription(request.getDescription());
        cropVariety.setClimate(request.getClimate());
        cropVariety.setYieldPotential(request.getYieldPotential());
        cropVariety.setCycleDurationDays(request.getCycleDurationDays());
        logger.info("Saving new crop variety: {} for crop: {} and crop variety: {}", request.getName(), crop.getName(), cropVariety);

        CropVariety savedVariety = cropVarietyRepository.save(cropVariety);
        logger.info("Successfully added new crop variety: {} with ID: {}", savedVariety.getName(), savedVariety.getId());

        return convertToCropVarietyResponse(savedVariety);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "farmerCropsByFarmerId", key = "#farmerId"),
            @CacheEvict(cacheNames = "currentFarmerCrops", allEntries = true),
            @CacheEvict(cacheNames = "currentFarmerProfile", allEntries = true),
            @CacheEvict(cacheNames = "farmersPortfolio", allEntries = true),
            @CacheEvict(cacheNames = "farmerProfileDetail", key = "#farmerId"),
            @CacheEvict(cacheNames = "consultantActiveSummary", allEntries = true),
            @CacheEvict(cacheNames = "farmerScheduleByVariety", allEntries = true)
    })
    public FarmerCropVarietyResponse assignCropVarietyToFarmer(Long farmerId, AssignCropVarietyRequest request) {
        Assert.notNull(farmerId, "Farmer ID is required");
        Assert.notNull(request, "Assignment request cannot be null");

        // Get farmer
        UserProfile farmer = userProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + farmerId));
        logger.info("Assigning crop variety to farmer: {} (ID: {})", farmer.getFirstName() + " " + farmer.getLastName(), farmerId);

        // Get crop variety
        CropVariety cropVariety = cropVarietyRepository.findById(request.getCropVarietyId())
                .orElseThrow(() -> new ResourceNotFoundException("Crop variety not found with ID: " + request.getCropVarietyId()));
        logger.info("Found crop variety: {} (ID: {}) for assignment", cropVariety.getName(), cropVariety.getId());

        // Create farmer crop variety assignment
        FarmerCropVariety farmerCropVariety = new FarmerCropVariety();
        farmerCropVariety.setFarmer(farmer);
        farmerCropVariety.setCropVariety(cropVariety);
        farmerCropVariety.setTotalLand(request.getTotalLand());
        farmerCropVariety.setTotalPlants(request.getTotalPlants());
        farmerCropVariety.setSowingDate(request.getSowingDate());
        farmerCropVariety.setExpectedHarvestDate(request.getExpectedHarvestDate());
        farmerCropVariety.setStatus(request.getStatus());
        logger.info("Saving farmer crop variety assignment for farmer ID: {} and crop variety ID: {} with farmerCropVariety: {}", farmerId, cropVariety.getId(), farmerCropVariety);

        FarmerCropVariety savedAssignment = farmerCropVarietyRepository.save(farmerCropVariety);
        logger.info("Successfully saved farmer crop variety assignment with ID: {}", savedAssignment.getId());

        return convertToFarmerCropVarietyResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "farmersPortfolio",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public List<FarmerPortfolioResponse> getFarmersPortfolio() {
        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Fetching farmers portfolio for consultant with mobile: {}", consultantMobile);

        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));

        // Get all farmers for this consultant
        return userProfileRepository.findByConsultantId(consultant.getUser().getId())
                .stream()
                .map(this::convertToFarmerPortfolioResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "farmerProfileDetail", key = "#farmerId")
    public FarmerProfileDetailResponse getFarmerProfileDetail(Long farmerId) {
        Assert.notNull(farmerId, "Farmer ID is required");

        UserProfile farmer = userProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + farmerId));
        logger.info("Fetching detailed profile for farmer: {} (ID: {})", farmer.getFirstName() + " " + farmer.getLastName(), farmerId);

        FarmerProfileDetailResponse response = new FarmerProfileDetailResponse();
        response.setId(farmer.getId());
        response.setFirstName(farmer.getFirstName());
        response.setLastName(farmer.getLastName());
        response.setEmail(farmer.getEmail());
        response.setMobile(farmer.getUser().getMobile());
        response.setPrimarySector(farmer.getSector());

        // Address info
        if (farmer.getAddress() != null) {
            logger.info("Fetching address info for farmer: {} (ID: {})", farmer.getFirstName() + " " + farmer.getLastName(), farmer.getId());
            response.setAddressLine(farmer.getAddress().getAddressLine());
            response.setCity(farmer.getAddress().getCity());
            response.setDistrict(farmer.getAddress().getDistrict());
            response.setState(farmer.getAddress().getState());
            response.setPostalCode(farmer.getAddress().getPostalCode());
        }

        // Consultant info
        if (farmer.getConsultant() != null) {
            logger.info("Fetching consultant info for farmer: {} (ID: {})", farmer.getFirstName() + " " + farmer.getLastName(), farmer.getId());
            response.setConsultantMobile(farmer.getConsultant().getMobile());
            // Try to get consultant profile details
            userProfileRepository.findByUserId(farmer.getConsultant().getId()).ifPresent(consultantProfile -> {
                response.setConsultantName(consultantProfile.getFirstName() + " " + consultantProfile.getLastName());
            });
        }

        response.setClientId(farmer.getId().toString());
        response.setJoinedDate(farmer.getCreatedAt() != null ? farmer.getCreatedAt().toString() : null);

        // Get assigned crops
        List<FarmerCropVarietyResponse> crops = getFarmerCrops(farmerId);
        response.setAssignedCrops(crops);
        logger.info("Fetched {} assigned crops for farmer: {} (ID: {})", crops.size(), farmer.getFirstName() + " " + farmer.getLastName(), farmerId);

        response.setCreatedAt(farmer.getCreatedAt() != null ? farmer.getCreatedAt().toString() : null);
        response.setUpdatedAt(farmer.getUpdatedAt() != null ? farmer.getUpdatedAt().toString() : null);

        logger.info("Returning detailed profile response for farmer: {} (ID: {})", farmer.getFirstName() + " " + farmer.getLastName(), farmerId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "farmerCropsByFarmerId", key = "#farmerId")
    public List<FarmerCropVarietyResponse> getFarmerCrops(Long farmerId) {
        Assert.notNull(farmerId, "Farmer ID is required");

        logger.info("Fetching assigned crops for farmer with ID: {}", farmerId);

        return farmerCropVarietyRepository.findByFarmerId(farmerId)
                .stream()
                .map(this::convertToFarmerCropVarietyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "currentFarmerCrops",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public List<FarmerCropVarietyResponse> getCurrentFarmerCrops() {
        String farmerMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Fetching assigned crops for current farmer with mobile: {}", farmerMobile);

        UserProfile farmer = userProfileRepository.findByUser_Mobile(farmerMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        return getFarmerCrops(farmer.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "farmerCropsByFarmerId", key = "#farmerId"),
            @CacheEvict(cacheNames = "currentFarmerCrops", allEntries = true),
            @CacheEvict(cacheNames = "currentFarmerProfile", allEntries = true),
            @CacheEvict(cacheNames = "farmersPortfolio", allEntries = true),
            @CacheEvict(cacheNames = "farmerProfileDetail", key = "#farmerId"),
            @CacheEvict(cacheNames = "consultantActiveSummary", allEntries = true),
            @CacheEvict(cacheNames = "farmerScheduleByVariety", allEntries = true)
    })
    public FarmerCropVarietyResponse updateFarmerCropVariety(Long farmerId, Long cropVarietyAssignmentId, AssignCropVarietyRequest request) {
        Assert.notNull(farmerId, "Farmer ID is required");
        Assert.notNull(cropVarietyAssignmentId, "Crop variety assignment ID is required");
        Assert.notNull(request, "Update request cannot be null");

        FarmerCropVariety assignment = farmerCropVarietyRepository.findById(cropVarietyAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + cropVarietyAssignmentId));
        logger.info("Updating farmer crop variety assignment with ID: {} for farmer ID: {}", cropVarietyAssignmentId, farmerId);

        // Verify ownership
        if (!assignment.getFarmer().getId().equals(farmerId)) {
            logger.error("Assignment ID: {} does not belong to farmer ID: {}", cropVarietyAssignmentId, farmerId);
            throw new ResourceNotFoundException("Assignment does not belong to this farmer");
        }

        assignment.setTotalLand(request.getTotalLand());
        assignment.setTotalPlants(request.getTotalPlants());
        assignment.setSowingDate(request.getSowingDate());
        assignment.setExpectedHarvestDate(request.getExpectedHarvestDate());
        assignment.setStatus(request.getStatus());
        logger.info("Saving updated farmer crop variety assignment with ID: {} for farmer ID: {} with updated assignment: {}", cropVarietyAssignmentId, farmerId, assignment);

        FarmerCropVariety updatedAssignment = farmerCropVarietyRepository.save(assignment);

        logger.info("Successfully updated farmer crop variety assignment with ID: {} for farmer ID: {}", updatedAssignment.getId(), farmerId);
        return convertToFarmerCropVarietyResponse(updatedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "consultantCropsWithVarieties",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public List<ConsultantCropVarietiesResponse> getConsultantCropsWithVarieties() {
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));
        logger.info("Fetching crops with varieties for consultant with mobile: {}", consultantMobile);

        List<CropVariety> consultantVarieties = cropVarietyRepository.findByConsultantId(consultant.getId());
        Map<Long, ConsultantCropVarietiesResponse> groupedByCrop = new LinkedHashMap<>();

        logger.info("Found {} crop varieties for consultant ID: {}", consultantVarieties.size(), consultant.getId());

        for (CropVariety variety : consultantVarieties) {
            if (variety.getCrop() == null || variety.getCrop().getId() == null) {
                continue;
            }

            Long cropId = variety.getCrop().getId();
            ConsultantCropVarietiesResponse cropGroup = groupedByCrop.computeIfAbsent(cropId, id ->
                    ConsultantCropVarietiesResponse.builder()
                            .cropId(id)
                            .cropName(variety.getCrop().getName())
                            .varieties(new ArrayList<>())
                            .build()
            );

            cropGroup.getVarieties().add(
                    ConsultantCropVarietiesResponse.VarietyInfo.builder()
                            .id(variety.getId())
                            .name(variety.getName())
                            .description(variety.getDescription())
                            .climate(variety.getClimate())
                            .yieldPotential(variety.getYieldPotential())
                            .cycleDurationDays(variety.getCycleDurationDays())
                            .createdAt(variety.getCreatedAt() != null ? variety.getCreatedAt().toString() : null)
                            .updatedAt(variety.getUpdatedAt() != null ? variety.getUpdatedAt().toString() : null)
                            .build()
            );
        }

        logger.info("Returning {} grouped crops with varieties for consultant ID: {}", groupedByCrop.size(), consultant.getId());
        return new ArrayList<>(groupedByCrop.values());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "consultantActiveSummary",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public ConsultantActiveSummaryResponse getConsultantActiveSummary() {
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));

        Long consultantUserId = consultant.getUser().getId();
        logger.info("Fetching active summary for consultant with ID: {}", consultantUserId);

        long totalActiveFarmers = userProfileRepository
                .countByConsultantIdAndUser_RoleAndUser_Status(consultantUserId, UserRole.FARMER, "ACTIVE");
        long totalActiveCropVarieties = farmerCropVarietyRepository.countActiveByConsultantUserId(consultantUserId);

        logger.info("Consultant ID: {} has {} active farmers and {} active crop varieties", consultantUserId, totalActiveFarmers, totalActiveCropVarieties);
        return ConsultantActiveSummaryResponse.builder()
                .totalActiveFarmers(totalActiveFarmers)
                .totalActiveCropVarieties(totalActiveCropVarieties)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "currentFarmerProfile",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public FarmerProfileResponse getCurrentFarmerProfile() {
        String farmerMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Fetching profile for current farmer with mobile: {}", farmerMobile);

        UserProfile farmer = userProfileRepository.findByUser_Mobile(farmerMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
        logger.info("Found farmer with ID: {}", farmer.getId());

        // Build farmer info
        FarmerProfileResponse.FarmerInfo farmerInfo = FarmerProfileResponse.FarmerInfo.builder()
                .farmerId(farmer.getId())
                .farmerName(farmer.getFirstName() + " " + farmer.getLastName())
                .farmerIdDisplay("#" + farmer.getId())
                .build();

        // Build personal info
        String farmSize = "10";
        String farmType = "N/A";
        logger.info("Fetching farm size and type for farmer with ID: {}", farmer.getId());

        List<FarmerCropVariety> farmerCrops = farmerCropVarietyRepository.findByFarmerId(farmer.getId());
        if (!farmerCrops.isEmpty()) {
            FarmerCropVariety firstCrop = farmerCrops.get(0);
            logger.info("Found {} crops for farmer ID: {}, using first crop variety ID: {} to determine farm size and type", farmerCrops.size(), farmer.getId(), firstCrop.getId());

            if (firstCrop.getTotalLand() != null) {
                farmSize = firstCrop.getTotalLand() + " Acres";
                logger.info("Determined farm size for farmer ID: {} is {}", farmer.getId(), farmSize);
            }
            if (firstCrop.getCropVariety() != null && firstCrop.getCropVariety().getCrop() != null) {
                farmType = firstCrop.getCropVariety().getCrop().getName();
                logger.info("Determined farm type for farmer ID: {} is {}", farmer.getId(), farmType);
            }
        }

        String addressLine = "N/A";
        String city = "N/A";
        String district = "N/A";
        String state = "N/A";
        String postalCode = "N/A";

        if (farmer.getAddress() != null) {
            addressLine = farmer.getAddress().getAddressLine();
            city = farmer.getAddress().getCity();
            district = farmer.getAddress().getDistrict();
            state = farmer.getAddress().getState();
            postalCode = farmer.getAddress().getPostalCode();
            logger.info("Fetched address info for farmer ID: {} - Address: {}, City: {}, District: {}, State: {}, Postal Code: {}", farmer.getId(), addressLine, city, district, state, postalCode);
        }

        FarmerProfileResponse.PersonalInfo personalInfo = FarmerProfileResponse.PersonalInfo.builder()
                .mobileNumber(farmer.getUser().getMobile())
                .farmSize(farmSize)
                .farmType(farmType)
                .primaryAddress(addressLine)
                .city(city)
                .district(district)
                .state(state)
                .postalCode(postalCode)
                .build();

        // Build consultant info
        FarmerProfileResponse.ConsultantInfo consultantInfo = null;
         if (farmer.getConsultant() != null) {
            // Load consultant's UserProfile from consultant User ID
             logger.info("Fetching consultant profile for farmer ID: {} with consultant ID: {}", farmer.getId(), farmer.getConsultant().getId());

            UserProfile consultantProfile = userProfileRepository.findByUserId(farmer.getConsultant().getId())
                    .orElse(null);
            
            if (consultantProfile != null) {
                consultantInfo = FarmerProfileResponse.ConsultantInfo.builder()
                        .consultantId(consultantProfile.getId())
                        .consultantName(consultantProfile.getFirstName() + " " + consultantProfile.getLastName())
                        .consultantTitle("Consultant")
                        .consultantMobile(farmer.getConsultant().getMobile())
                        .consultantEmail(consultantProfile.getEmail())
                        .build();
                logger.info("Fetched consultant info for farmer ID: {} - Consultant Name: {}, Mobile: {}, Email: {}", farmer.getId(), consultantInfo.getConsultantName(), consultantInfo.getConsultantMobile(), consultantInfo.getConsultantEmail());
            }
        }

         logger.info("Returning FarmerProfileResponse for farmer ID: {} with farmerInfo: {}, personalInfo: {}, consultantInfo: {}", farmer.getId(), farmerInfo, personalInfo, consultantInfo);
        return FarmerProfileResponse.builder()
                .farmerInfo(farmerInfo)
                .personalInfo(personalInfo)
                .consultantInfo(consultantInfo)
                .build();
    }

    private CropVarietyResponse convertToCropVarietyResponse(CropVariety cropVariety) {
        String consultantName = "N/A";
        logger.info("Converting CropVariety entity to CropVarietyResponse DTO for crop variety ID: {}", cropVariety.getId());

        if (cropVariety.getConsultant() != null) {
            consultantName = cropVariety.getConsultant().getFirstName() + " " + cropVariety.getConsultant().getLastName();
            logger.info("Fetched consultant name for crop variety ID: {} - Consultant Name: {}", cropVariety.getId(), consultantName);
        }

        String cropName = "N/A";
        if (cropVariety.getCrop() != null) {
            cropName = cropVariety.getCrop().getName();
            logger.info("Fetched crop name for crop variety ID: {} - Crop Name: {}", cropVariety.getId(), cropName);
        }

        logger.info("Returning CropVarietyResponse for crop variety ID: {} with cropName: {}, consultantName: {}", cropVariety.getId(), cropName, consultantName);
        return CropVarietyResponse.builder()
                .id(cropVariety.getId())
                .cropId(cropVariety.getCrop() != null ? cropVariety.getCrop().getId() : null)
                .cropName(cropName)
                .consultantId(cropVariety.getConsultant() != null ? cropVariety.getConsultant().getId() : null)
                .consultantName(consultantName)
                .name(cropVariety.getName())
                .description(cropVariety.getDescription())
                .climate(cropVariety.getClimate())
                .yieldPotential(cropVariety.getYieldPotential())
                .cycleDurationDays(cropVariety.getCycleDurationDays())
                .createdAt(cropVariety.getCreatedAt() != null ? cropVariety.getCreatedAt().toString() : null)
                .updatedAt(cropVariety.getUpdatedAt() != null ? cropVariety.getUpdatedAt().toString() : null)
                .build();
    }

    private FarmerCropVarietyResponse convertToFarmerCropVarietyResponse(FarmerCropVariety farmerCropVariety) {
        String cropVarietyName = "N/A";
        String cropName = "N/A";
        String yieldPotential = "N/A";
        Long cycleDurationDays = 0L;
        Long lastSentDay = 0L;
        logger.info("Converting FarmerCropVariety entity to FarmerCropVarietyResponse DTO for assignment ID: {}", farmerCropVariety.getId());

        if (farmerCropVariety.getCropVariety() != null) {
            cropVarietyName = farmerCropVariety.getCropVariety().getName();
            yieldPotential = farmerCropVariety.getCropVariety().getYieldPotential();
            cycleDurationDays = farmerCropVariety.getCropVariety().getCycleDurationDays();
            logger.info("Fetched crop variety info for assignment ID: {} - Crop Variety Name: {}, Yield Potential: {}, Cycle Duration Days: {}", farmerCropVariety.getId(), cropVarietyName, yieldPotential, cycleDurationDays);
            lastSentDay = farmerCropVarietyScheduleRepository.getLastSentDayByFarmerCropVarietyId(farmerCropVariety.getId());
            if (farmerCropVariety.getCropVariety().getCrop() != null) {
                cropName = farmerCropVariety.getCropVariety().getCrop().getName();
                logger.info("Fetched crop name for assignment ID: {} - Crop Name: {}", farmerCropVariety.getId(), cropName);
            }
        }

        // Calculate progress percentage based on dates
        int progressPercentage = 0;
        if (farmerCropVariety.getSowingDate() != null && farmerCropVariety.getExpectedHarvestDate() != null) {
            LocalDate today = LocalDate.now();
            LocalDate sowingDate = farmerCropVariety.getSowingDate();
            LocalDate harvestDate = farmerCropVariety.getExpectedHarvestDate();
            logger.info("Calculating progress percentage for assignment ID: {} - Sowing Date: {}, Expected Harvest Date: {}, Today: {}", farmerCropVariety.getId(), sowingDate, harvestDate, today);

            if (today.isBefore(sowingDate)) {
                progressPercentage = 0;
                logger.info("Today is before sowing date for assignment ID: {}, setting progress percentage to 0", farmerCropVariety.getId());

            } else if (today.isAfter(harvestDate)) {
                progressPercentage = 100;
                logger.info("Today is after expected harvest date for assignment ID: {}, setting progress percentage to 100", farmerCropVariety.getId());

            } else {
                long totalDays = ChronoUnit.DAYS.between(sowingDate, harvestDate);
                long daysElapsed = ChronoUnit.DAYS.between(sowingDate, today);
                progressPercentage = totalDays > 0 ? (int) ((daysElapsed * 100) / totalDays) : 0;
                logger.info("Calculated progress percentage for assignment ID: {} - Total Days: {}, Days Elapsed: {}, Progress Percentage: {}", farmerCropVariety.getId(), totalDays, daysElapsed, progressPercentage);
            }
        }

        logger.info("Returning FarmerCropVarietyResponse for assignment ID: {} with cropVarietyName: {}, cropName: {}, progressPercentage: {}, yieldPotential: {}, cycleDurationDays: {}", farmerCropVariety.getId(), cropVarietyName, cropName, progressPercentage, yieldPotential, cycleDurationDays);
        return FarmerCropVarietyResponse.builder()
                .id(farmerCropVariety.getId())
                .cropVarietyId(farmerCropVariety.getCropVariety() != null ? farmerCropVariety.getCropVariety().getId() : null)
                .cropVarietyName(cropVarietyName)
                .cropName(cropName)
                .totalLand(farmerCropVariety.getTotalLand())
                .totalPlants(farmerCropVariety.getTotalPlants())
                .sowingDate(farmerCropVariety.getSowingDate() != null ? farmerCropVariety.getSowingDate().toString() : null)
                .lastScheduleSentDay(lastSentDay != null ? lastSentDay : 0)
                .expectedHarvestDate(farmerCropVariety.getExpectedHarvestDate() != null ? farmerCropVariety.getExpectedHarvestDate().toString() : null)
                .status(farmerCropVariety.getStatus())
                .progressPercentage(progressPercentage)
                .yieldPotential(yieldPotential)
                .cycleDurationDays(cycleDurationDays)
                .createdAt(farmerCropVariety.getCreatedAt() != null ? farmerCropVariety.getCreatedAt().toString() : null)
                .updatedAt(farmerCropVariety.getUpdatedAt() != null ? farmerCropVariety.getUpdatedAt().toString() : null)
                .build();
    }

    private FarmerPortfolioResponse convertToFarmerPortfolioResponse(UserProfile farmer) {
        logger.info("Converting UserProfile entity to FarmerPortfolioResponse DTO for farmer ID: {}", farmer.getId());

        List<String> crops = farmerCropVarietyRepository.findByFarmerId(farmer.getId())
                .stream()
                .map(fv -> fv.getCropVariety() != null ? fv.getCropVariety().getName() : "N/A")
                .collect(Collectors.toList());

        logger.info("Fetched {} crops for farmer ID: {}", crops.size(), farmer.getId());
        String location = "N/A";
        if (farmer.getAddress() != null) {
            location = farmer.getAddress().getCity() + ", " + farmer.getAddress().getState();
        }
        logger.info("Determined location for farmer ID: {} - Location: {}", farmer.getId(), location);

        return FarmerPortfolioResponse.builder()
                .id(farmer.getId())
                .firstName(farmer.getFirstName())
                .lastName(farmer.getLastName())
                .email(farmer.getEmail())
                .mobile(farmer.getUser().getMobile())
                .location(location)
                .primarySector(farmer.getSector())
                .crops(crops)
                .createdAt(farmer.getCreatedAt() != null ? farmer.getCreatedAt().toString() : null)
                .updatedAt(farmer.getUpdatedAt() != null ? farmer.getUpdatedAt().toString() : null)
                .build();
    }
}


