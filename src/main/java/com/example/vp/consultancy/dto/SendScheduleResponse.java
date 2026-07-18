package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendScheduleResponse {
    private Long farmerId;
    private Long varietyId;
    private Long scheduleId;
    private Long daysSent;
    private Long startDay;
    private Long endDay;
    private String message;
}
