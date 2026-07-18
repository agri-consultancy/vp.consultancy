package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.dto.ApiResponse;
import com.example.vp.consultancy.dto.FarmerCropVarietyResponse;
import com.example.vp.consultancy.dto.FarmerScheduleResponse;
import com.example.vp.consultancy.dto.ConsultantAdvertisementResponse;
import com.example.vp.consultancy.dto.FarmerProfileResponse;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.service.CropVarietyService;
import com.example.vp.consultancy.service.SendScheduleService;
import com.example.vp.consultancy.service.ConsultantAdvertisementService;
import com.example.vp.consultancy.util.Util;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/farmer")
@PreAuthorize("hasRole('FARMER')")
@Validated
@RequiredArgsConstructor
public class FarmerController {

    private final SendScheduleService sendScheduleService;
    private final CropVarietyService cropVarietyService;
    private final ConsultantAdvertisementService advertisementService;

    @GetMapping("/crop-varieties")
    public ResponseEntity<ApiResponse<List<FarmerCropVarietyResponse>>> getFarmerCropVarieties() {
        List<FarmerCropVarietyResponse> cropVarieties = cropVarietyService.getCurrentFarmerCrops();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Farmer crop varieties retrieved successfully",
                cropVarieties,
                HttpStatus.OK.value()
        ));
    }

    @GetMapping("/schedules/{farmerCropVarietyId}")
    public ResponseEntity<ApiResponse<FarmerScheduleResponse>> getFarmerSchedules(
            @PathVariable @Positive Long farmerCropVarietyId) {
        UserProfile userProfile = Util.getCurrentUserProfile();
        FarmerScheduleResponse response = sendScheduleService.getFarmerSchedule(userProfile,farmerCropVarietyId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Farmer schedules retrieved successfully",
                response,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Retrieves all consultant advertisements to display on the farmer's main page.
     *
     * Only accessible to users with FARMER role.
     * Read-only operation.
     *
     * Business Logic:
     * 1. Retrieves all advertisements from all consultants
     * 2. Orders advertisements by priority (highest priority first)
     * 3. Returns list of advertisements for display on main page
     *
     * @return ResponseEntity with ApiResponse containing list of advertisements ordered by priority
     */
    @GetMapping("/consultant-advertisements")
    public ResponseEntity<ApiResponse<List<ConsultantAdvertisementResponse>>> getConsultantAdvertisements() {
        List<ConsultantAdvertisementResponse> advertisements = advertisementService.getAllAdvertisements();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Consultant advertisements retrieved successfully",
                advertisements,
                HttpStatus.OK.value()
        ));
    }

    /**
     * Retrieves the current farmer's profile information including personal details and consultant info.
     *
     * Only accessible to users with FARMER role.
     * Farmer ID is extracted from the JWT access token.
     * Read-only operation.
     *
     * @return ResponseEntity with ApiResponse containing farmer profile with personal info and consultant details
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<FarmerProfileResponse>> getFarmerProfile() {
        FarmerProfileResponse profile = cropVarietyService.getCurrentFarmerProfile();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Farmer profile retrieved successfully",
                profile,
                HttpStatus.OK.value()
        ));
    }
}
