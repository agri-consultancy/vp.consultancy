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
public class UpdateScheduleDayRequest {

    @NotBlank(message = "dayTitle is required")
    private String dayTitle;

    private String dayDescription;

    private String status;

    private Long displayOrder;
}
