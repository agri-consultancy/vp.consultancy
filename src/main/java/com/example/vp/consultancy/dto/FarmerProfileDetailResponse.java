package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * Farmer Profile Detail Response DTO - Detailed farmer information with crops
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerProfileDetailResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String mobile;

    private String primarySector;

    private String profileImageUrl;

    private String organizationName;

    // Contact details
    private String addressLine;

    private String city;

    private String district;

    private String state;

    private String postalCode;

    // Farm information
    private String consultantName;

    private String consultantMobile;

    private String joinedDate;

    private String clientId;

    private List<FarmerCropVarietyResponse> assignedCrops;

    private String createdAt;

    private String updatedAt;
}

