package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Farmer Crop Variety Response DTO - Displays crop variety details assigned to a farmer
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerCropVarietyResponse {

    private Long id;

    private Long cropVarietyId;

    private String cropVarietyName;

    private String cropName;

    private String cropIcon;

    private Double totalLand;

    private Integer totalPlants;

    private String sowingDate;

    private Long lastScheduleSentDay;

    private String expectedHarvestDate;

    private String status;

    private Integer progressPercentage;

    private String yieldPotential;

    private Long cycleDurationDays;

    private String createdAt;

    private String updatedAt;
}

