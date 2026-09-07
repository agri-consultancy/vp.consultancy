package com.example.vp.consultancy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddScheduleTaskRequest {

    private String taskType;

    private String taskDescription;

    private String fertilizerName;

    @NotBlank(message = "quantity is required")
    private String quantity;

    private String proportion;

    private Long priority;
}
