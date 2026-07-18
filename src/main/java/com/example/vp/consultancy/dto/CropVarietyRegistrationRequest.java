package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

/**
 * Crop Variety Registration Request DTO - For consultants to add new crop varieties
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CropVarietyRegistrationRequest {

    @NotNull(message = "Crop ID is required")
    private Long cropId;

    @NotBlank(message = "Variety name is required")
    @Size(min = 2, max = 255, message = "Variety name must be between 2 and 255 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Climate preference is required")
    @Pattern(regexp = "^(Tropical|Temperate|Arid|Subtropical)$", message = "Climate must be one of: Tropical, Temperate, Arid, Subtropical")
    private String climate;

    @NotBlank(message = "Yield potential is required")
    @Size(min = 3, max = 255, message = "Yield potential must be between 3 and 255 characters")
    private String yieldPotential;

    @NotNull(message = "Cycle duration is required")
    @Min(value = 1, message = "Cycle duration must be at least 1 day")
    @Max(value = 365, message = "Cycle duration must not exceed 365 days")
    private Long cycleDurationDays;
}

