package com.example.vp.consultancy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class SendScheduleDayRequest {

    @NotNull(message = "dayNumber is required")
    @Positive(message = "dayNumber must be positive")
    private Long dayNumber;

    @NotBlank(message = "dayTitle is required")
    private String dayTitle;

    private String dayDescription;

    private String status;

    private Long displayOrder;

    @NotEmpty(message = "tasks are required")
    @Valid
    private List<SendScheduleTaskRequest> tasks;
}
