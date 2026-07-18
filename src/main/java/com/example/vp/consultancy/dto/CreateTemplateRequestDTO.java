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
public class CreateTemplateRequestDTO {
    @NotNull(message = "Crop variety ID is required")
    private Long cropVarietyId;
    
    @NotNull(message = "Version is required")
    private Long version;
    
    private String description;
    
    @NotBlank(message = "Status is required")
    private String status;
}
