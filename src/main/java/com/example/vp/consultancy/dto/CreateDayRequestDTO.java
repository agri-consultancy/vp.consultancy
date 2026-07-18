package com.example.vp.consultancy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDayRequestDTO {
    @NotNull(message = "Day number is required")
    private Long dayNumber;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private Long displayOrder;
}
