package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditScheduleDayResponse {
    private Long farmerId;
    private Long farmerCropVarietyId;
    private Long dayNumber;
    private String message;
    private ScheduleDayDTO dayDetails;
}
