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
public class ScheduleDayDTO {
    private Long dayNumber;
    private String dayTitle;
    private String dayDescription;
    private List<ScheduleTaskDTO> tasks;
}
