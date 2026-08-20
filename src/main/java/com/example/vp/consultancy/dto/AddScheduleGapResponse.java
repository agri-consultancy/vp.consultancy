package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddScheduleGapResponse {
    private Long farmerId;
    private Long farmerCropVarietyId;
    private Long gapDays;
    private Long totalGapDays;
    private Long lastSentFarmerDay;
    private Long lastSentMasterDay;
    private Long nextFarmerDay;
    private Long nextMasterDay;
    private String message;
}

