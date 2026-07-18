package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Login Response DTO - Returns JWT tokens after successful authentication
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /** JWT Access Token for API calls */
    private String accessToken;

    /** JWT Refresh Token for obtaining new access tokens */
    private String refreshToken;

    /** Token type (Bearer) */
    private String tokenType;

    /** Access token expiration time in milliseconds */
    private Long expiresIn;

    /** User role */
    private String role;

    /** User mobile number */
    private String mobile;

    /** User information object */
    private UserDetailsDto userDetails;
}
