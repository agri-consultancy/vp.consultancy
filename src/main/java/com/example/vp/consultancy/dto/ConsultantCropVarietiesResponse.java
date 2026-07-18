package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for consultant crops with their created varieties.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantCropVarietiesResponse {

    private Long cropId;
    private String cropName;
    private List<VarietyInfo> varieties;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VarietyInfo {
        private Long id;
        private String name;
        private String description;
        private String climate;
        private String yieldPotential;
        private Long cycleDurationDays;
        private String createdAt;
        private String updatedAt;
    }
}
