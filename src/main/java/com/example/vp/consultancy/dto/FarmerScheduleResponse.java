package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerScheduleResponse {
    private FarmerDetails farmer;
    private CropVarietyDetails cropVariety;
    private List<AssignedScheduleDTO> schedules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FarmerDetails {
        private Long farmerId;
        private String farmerName;
        private String mobile;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CropVarietyDetails {
        private Long farmerCropVarietyId;
        private Long cropVarietyId;
        private String cropName;
        private String cropVarietyName;
        private String status;
        private LocalDate sowingDate;
        private LocalDate expectedHarvestDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignedScheduleDTO {
        private Long scheduleId;
        private LocalDate startDate;
        private Long startDay;
        private Long endDay;
        private Long daysSent;
        private List<ScheduleDayDTO> scheduleDays;
    }
}
