package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleTaskDTO {
    private Long taskId;
    private Long priority;
    private String taskType;
    private String taskDescription;
    private String fertilizerName;
    private String quantity;
    private String proportion;
}
