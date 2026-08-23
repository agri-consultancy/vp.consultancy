package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Assign Crop Variety Request DTO - For assigning a crop variety to a farmer
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignCropVarietyRequest {

    @NotNull(message = "Crop variety ID is required")
    private Long cropVarietyId;

    @NotNull(message = "Total plant to plant spacing is required")
    @Min(value = 0, message = "Total plant to plant spacing must be greater than 0")
    private Double plantToPlantSpacing;

    @NotNull(message = "Total row to row spacing is required")
    @Min(value = 0, message = "Total row to row spacing must be greater than 0")
    private Double rowToRowSpacing;

    @Min(value = 0, message = "Total plants must be greater than or equal to 0")
    private Integer totalPlants;

    @NotNull(message = "Sowing date is required")
    private LocalDate sowingDate;

    @NotNull(message = "Expected harvest date is required")
    private LocalDate expectedHarvestDate;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(Preparing|Active|Harvesting|Completed)$", message = "Status must be one of: Preparing, Active, Harvesting, Completed")
    private String status;
}

