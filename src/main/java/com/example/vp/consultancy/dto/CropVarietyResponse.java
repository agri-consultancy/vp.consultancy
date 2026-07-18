package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Crop Variety Response DTO - Crop variety information returned in API responses
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropVarietyResponse {

    private Long id;

    private Long cropId;

    private String cropName;

    private Long consultantId;

    private String consultantName;

    private String name;

    private String description;

    private String climate;

    private String yieldPotential;

    private Long cycleDurationDays;

    private String createdAt;

    private String updatedAt;
}

