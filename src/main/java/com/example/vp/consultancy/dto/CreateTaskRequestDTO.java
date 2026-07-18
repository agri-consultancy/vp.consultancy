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
public class CreateTaskRequestDTO {
    @NotBlank(message = "Fertilizer name is required")
    private String fertilizerName;
    
    @NotBlank(message = "Quantity is required")
    private String quantity;
    
    private String proportion;
    
    private Long priority;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String taskType;
}
