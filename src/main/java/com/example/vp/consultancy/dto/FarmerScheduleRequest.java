package com.example.vp.consultancy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerScheduleRequest {

    @NotNull(message = "farmerId is required")
    @Positive(message = "farmerId must be positive")
    private Long farmerId;

    @NotNull(message = "farmerCropVarietyId is required")
    @Positive(message = "farmerCropVarietyId must be positive")
    private Long farmerCropVarietyId;
}
