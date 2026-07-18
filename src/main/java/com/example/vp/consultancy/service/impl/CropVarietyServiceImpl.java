package com.example.vp.consultancy.service.impl;

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
import com.example.vp.consultancy.repository.CropRepository;
import com.example.vp.consultancy.repository.CropVarietyRepository;
import com.example.vp.consultancy.repository.FarmerCropVarietyRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.service.CropVarietyService;
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

    private final CropVarietyRepository cropVarietyRepository;
    private final FarmerCropVarietyRepository farmerCropVarietyRepository;
    private final CropRepository cropRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public CropVarietyServiceImpl(CropVarietyRepository cropVarietyRepository,
                                 FarmerCropVarietyRepository farmerCropVarietyRepository,
                                 CropRepository cropRepository,
                                 UserProfileRepository userProfileRepository,
                                 UserRepository userRepository) {
        this.cropVarietyRepository = cropVarietyRepository;
        this.farmerCropVarietyRepository = farmerCropVarietyRepository;
        this.cropRepository = cropRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CropVarietyResponse addCropVariety(CropVarietyRegistrationRequest request) {
        Assert.notNull(request, "Crop variety registration request cannot be null");
        Assert.notNull(request.getCropId(), "Crop ID is required");
        Assert.hasText(request.getName(), "Variety name is required");

        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));

        // Get crop
        Crop crop = cropRepository.findById(request.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with ID: " + request.getCropId()));

        // Create crop variety
        CropVariety cropVariety = new CropVariety();
        cropVariety.setCrop(crop);
        cropVariety.setConsultant(consultant);
        cropVariety.setName(request.getName());
        cropVariety.setDescription(request.getDescription());
        cropVariety.setClimate(request.getClimate());
        cropVariety.setYieldPotential(request.getYieldPotential());
        cropVariety.setCycleDurationDays(request.getCycleDurationDays());

        CropVariety savedVariety = cropVarietyRepository.save(cropVariety);

        return convertToCropVarietyResponse(savedVariety);
    }

    @Override
    public FarmerCropVarietyResponse assignCropVarietyToFarmer(Long farmerId, AssignCropVarietyRequest request) {
        Assert.notNull(farmerId, "Farmer ID is required");
        Assert.notNull(request, "Assignment request cannot be null");

        // Get farmer
        UserProfile farmer = userProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + farmerId));

        // Get crop variety
        CropVariety cropVariety = cropVarietyRepository.findById(request.getCropVarietyId())
                .orElseThrow(() -> new ResourceNotFoundException("Crop variety not found with ID: " + request.getCropVarietyId()));

        // Create farmer crop variety assignment
        FarmerCropVariety farmerCropVariety = new FarmerCropVariety();
        farmerCropVariety.setFarmer(farmer);
        farmerCropVariety.setCropVariety(cropVariety);
        farmerCropVariety.setTotalLand(request.getTotalLand());
        farmerCropVariety.setTotalPlants(request.getTotalPlants());
        farmerCropVariety.setSowingDate(request.getSowingDate());
        farmerCropVariety.setExpectedHarvestDate(request.getExpectedHarvestDate());
        farmerCropVariety.setStatus(request.getStatus());

        FarmerCropVariety savedAssignment = farmerCropVarietyRepository.save(farmerCropVariety);

        return convertToFarmerCropVarietyResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmerPortfolioResponse> getFarmersPortfolio() {
        // Get current consultant
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
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
    public FarmerProfileDetailResponse getFarmerProfileDetail(Long farmerId) {
        Assert.notNull(farmerId, "Farmer ID is required");

        UserProfile farmer = userProfileRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + farmerId));

        FarmerProfileDetailResponse response = new FarmerProfileDetailResponse();
        response.setId(farmer.getId());
        response.setFirstName(farmer.getFirstName());
        response.setLastName(farmer.getLastName());
        response.setEmail(farmer.getEmail());
        response.setMobile(farmer.getUser().getMobile());
        response.setPrimarySector(farmer.getSector());

        // Address info
        if (farmer.getAddress() != null) {
            response.setAddressLine(farmer.getAddress().getAddressLine());
            response.setCity(farmer.getAddress().getCity());
            response.setDistrict(farmer.getAddress().getDistrict());
            response.setState(farmer.getAddress().getState());
            response.setPostalCode(farmer.getAddress().getPostalCode());
        }

        // Consultant info
        if (farmer.getConsultant() != null) {
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

        response.setCreatedAt(farmer.getCreatedAt() != null ? farmer.getCreatedAt().toString() : null);
        response.setUpdatedAt(farmer.getUpdatedAt() != null ? farmer.getUpdatedAt().toString() : null);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmerCropVarietyResponse> getFarmerCrops(Long farmerId) {
        Assert.notNull(farmerId, "Farmer ID is required");

        return farmerCropVarietyRepository.findByFarmerId(farmerId)
                .stream()
                .map(this::convertToFarmerCropVarietyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmerCropVarietyResponse> getCurrentFarmerCrops() {
        String farmerMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile farmer = userProfileRepository.findByUser_Mobile(farmerMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        return getFarmerCrops(farmer.getId());
    }

    @Override
    public FarmerCropVarietyResponse updateFarmerCropVariety(Long farmerId, Long cropVarietyAssignmentId, AssignCropVarietyRequest request) {
        Assert.notNull(farmerId, "Farmer ID is required");
        Assert.notNull(cropVarietyAssignmentId, "Crop variety assignment ID is required");
        Assert.notNull(request, "Update request cannot be null");

        FarmerCropVariety assignment = farmerCropVarietyRepository.findById(cropVarietyAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + cropVarietyAssignmentId));

        // Verify ownership
        if (!assignment.getFarmer().getId().equals(farmerId)) {
            throw new ResourceNotFoundException("Assignment does not belong to this farmer");
        }

        assignment.setTotalLand(request.getTotalLand());
        assignment.setTotalPlants(request.getTotalPlants());
        assignment.setSowingDate(request.getSowingDate());
        assignment.setExpectedHarvestDate(request.getExpectedHarvestDate());
        assignment.setStatus(request.getStatus());

        FarmerCropVariety updatedAssignment = farmerCropVarietyRepository.save(assignment);

        return convertToFarmerCropVarietyResponse(updatedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultantCropVarietiesResponse> getConsultantCropsWithVarieties() {
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));

        List<CropVariety> consultantVarieties = cropVarietyRepository.findByConsultantId(consultant.getId());
        Map<Long, ConsultantCropVarietiesResponse> groupedByCrop = new LinkedHashMap<>();

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

        return new ArrayList<>(groupedByCrop.values());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultantActiveSummaryResponse getConsultantActiveSummary() {
        String consultantMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile consultant = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));

        Long consultantUserId = consultant.getUser().getId();
        long totalActiveFarmers = userProfileRepository
                .countByConsultantIdAndUser_RoleAndUser_Status(consultantUserId, UserRole.FARMER, "ACTIVE");
        long totalActiveCropVarieties = farmerCropVarietyRepository.countActiveByConsultantUserId(consultantUserId);

        return ConsultantActiveSummaryResponse.builder()
                .totalActiveFarmers(totalActiveFarmers)
                .totalActiveCropVarieties(totalActiveCropVarieties)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FarmerProfileResponse getCurrentFarmerProfile() {
        String farmerMobile = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile farmer = userProfileRepository.findByUser_Mobile(farmerMobile)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        // Build farmer info
        FarmerProfileResponse.FarmerInfo farmerInfo = FarmerProfileResponse.FarmerInfo.builder()
                .farmerId(farmer.getId())
                .farmerName(farmer.getFirstName() + " " + farmer.getLastName())
                .farmerIdDisplay("#" + farmer.getId())
                .build();

        // Build personal info
        String farmSize = "10";
        String farmType = "N/A";
        List<FarmerCropVariety> farmerCrops = farmerCropVarietyRepository.findByFarmerId(farmer.getId());
        if (!farmerCrops.isEmpty()) {
            FarmerCropVariety firstCrop = farmerCrops.get(0);
            if (firstCrop.getTotalLand() != null) {
                farmSize = firstCrop.getTotalLand() + " Acres";
            }
            if (firstCrop.getCropVariety() != null && firstCrop.getCropVariety().getCrop() != null) {
                farmType = firstCrop.getCropVariety().getCrop().getName();
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
            }
        }

        return FarmerProfileResponse.builder()
                .farmerInfo(farmerInfo)
                .personalInfo(personalInfo)
                .consultantInfo(consultantInfo)
                .build();
    }

    private CropVarietyResponse convertToCropVarietyResponse(CropVariety cropVariety) {
        String consultantName = "N/A";
        if (cropVariety.getConsultant() != null) {
            consultantName = cropVariety.getConsultant().getFirstName() + " " + cropVariety.getConsultant().getLastName();
        }

        String cropName = "N/A";
        if (cropVariety.getCrop() != null) {
            cropName = cropVariety.getCrop().getName();
        }

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

        if (farmerCropVariety.getCropVariety() != null) {
            cropVarietyName = farmerCropVariety.getCropVariety().getName();
            yieldPotential = farmerCropVariety.getCropVariety().getYieldPotential();
            cycleDurationDays = farmerCropVariety.getCropVariety().getCycleDurationDays();

            if (farmerCropVariety.getCropVariety().getCrop() != null) {
                cropName = farmerCropVariety.getCropVariety().getCrop().getName();
            }
        }

        // Calculate progress percentage based on dates
        int progressPercentage = 0;
        if (farmerCropVariety.getSowingDate() != null && farmerCropVariety.getExpectedHarvestDate() != null) {
            LocalDate today = LocalDate.now();
            LocalDate sowingDate = farmerCropVariety.getSowingDate();
            LocalDate harvestDate = farmerCropVariety.getExpectedHarvestDate();

            if (today.isBefore(sowingDate)) {
                progressPercentage = 0;
            } else if (today.isAfter(harvestDate)) {
                progressPercentage = 100;
            } else {
                long totalDays = ChronoUnit.DAYS.between(sowingDate, harvestDate);
                long daysElapsed = ChronoUnit.DAYS.between(sowingDate, today);
                progressPercentage = totalDays > 0 ? (int) ((daysElapsed * 100) / totalDays) : 0;
            }
        }

        return FarmerCropVarietyResponse.builder()
                .id(farmerCropVariety.getId())
                .cropVarietyId(farmerCropVariety.getCropVariety() != null ? farmerCropVariety.getCropVariety().getId() : null)
                .cropVarietyName(cropVarietyName)
                .cropName(cropName)
                .totalLand(farmerCropVariety.getTotalLand())
                .totalPlants(farmerCropVariety.getTotalPlants())
                .sowingDate(farmerCropVariety.getSowingDate() != null ? farmerCropVariety.getSowingDate().toString() : null)
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
        List<String> crops = farmerCropVarietyRepository.findByFarmerId(farmer.getId())
                .stream()
                .map(fv -> fv.getCropVariety() != null ? fv.getCropVariety().getName() : "N/A")
                .collect(Collectors.toList());

        String location = "N/A";
        if (farmer.getAddress() != null) {
            location = farmer.getAddress().getCity() + ", " + farmer.getAddress().getState();
        }

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


