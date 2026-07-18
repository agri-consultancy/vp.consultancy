package com.example.vp.consultancy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendScheduleTaskRequest {

    private String taskType;

    @NotBlank(message = "taskDescription is required")
    private String taskDescription;

    private String fertilizerName;

    @NotBlank(message = "quantity is required")
    private String quantity;

    @NotBlank(message = "proportion is required")
    private String proportion;

    @PositiveOrZero(message = "priority must be zero or positive")
    private Long priority;
}
