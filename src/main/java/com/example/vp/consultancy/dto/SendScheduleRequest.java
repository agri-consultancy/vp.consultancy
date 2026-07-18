package com.example.vp.consultancy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendScheduleRequest {

    @NotNull(message = "farmerId is required")
    @Positive(message = "farmerId must be positive")
    private Long farmerId;

    @NotNull(message = "farmerCropVarietyId is required")
    @Positive(message = "farmerCropVarietyId must be positive")
    private Long farmerCropVarietyId;

    @NotNull(message = "numberOfDays is required")
    @Positive(message = "numberOfDays must be positive")
    private Long numberOfDays;

    @NotEmpty(message = "scheduleDays are required")
    @Valid
    private List<SendScheduleDayRequest> scheduleDays;
}
