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
public class GetNextSchedulePreviewResponse {
    private Long farmerId;
    private Long farmerCropVarietyId;
    private Long masterScheduleTemplateId;
    private Long startDay;
    private Long endDay;
    private Long masterStartDay;
    private Long masterEndDay;
    private Long totalDays;
    private List<ScheduleDayDTO> scheduleDays;
}
