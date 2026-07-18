package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerProfileResponse {
    private FarmerInfo farmerInfo;
    private PersonalInfo personalInfo;
    private ConsultantInfo consultantInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FarmerInfo {
        private Long farmerId;
        private String farmerName;
        private String farmerIdDisplay;
        private Boolean verified;
        private String bio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonalInfo {
        private String mobileNumber;
        private String farmSize;
        private String farmType;
        private String primaryAddress;
        private String city;
        private String district;
        private String state;
        private String postalCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsultantInfo {
        private Long consultantId;
        private String consultantName;
        private String consultantTitle;
        private String consultantMobile;
        private String consultantEmail;
    }
}
