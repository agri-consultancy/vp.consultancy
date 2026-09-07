package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditScheduleTaskResponse {
    private Long farmerId;
    private Long farmerCropVarietyId;
    private Long dayNumber;
    private Long taskId;
    private String operation;
    private String message;
    private ScheduleTaskDTO taskDetails;
}
