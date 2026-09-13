package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;

/**
 * Update Farmer Details Request DTO - For consultants to update farmer details
 * Mobile number cannot be updated
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFarmerDetailsRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    private String addressLine;

    private String city;

    private String district;

    private String state;

    private String postalCode;

    @Size(min = 2, max = 100, message = "Sector must be between 2 and 100 characters")
    private String sector;
}

