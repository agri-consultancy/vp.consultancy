package com.example.vp.consultancy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User Response DTO - User information returned in API responses
 * Excludes sensitive information like passwords
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String mobile;

    private String role;

    private String status;

    private String firstName;

    private String lastName;

    private String email;

    @JsonProperty("mobileVerified")
    private Boolean isMobileVerified;

    @JsonProperty("emailVerified")
    private Boolean isEmailVerified;

    private String createdAt;
}
