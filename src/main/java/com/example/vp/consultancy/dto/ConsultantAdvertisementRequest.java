package com.example.vp.consultancy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consultant Advertisement Request DTO - Input for creating or updating advertisements
 * Used when a consultant creates a new advertisement to show their farmers
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantAdvertisementRequest {

    @NotBlank(message = "URL cannot be blank")
    private String url;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String descriptions;

    @NotNull(message = "Priority cannot be null")
    @Min(value = 0, message = "Priority must be a non-negative number")
    private Long priority;

    @NotBlank(message = "Type cannot be blank")
    @Pattern(regexp = "^(?i)(image|video|text)$", message = "Type must be one of: image, video, text")
    private String type;
}
