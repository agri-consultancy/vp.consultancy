package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detailed user information returned by /api/auth/me
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
    private Long id;
    private String mobile;
    private String role;
    private String status;
    private String firstName;
    private String lastName;
    private String email;

    private AddressDto address;

    // If this user is a farmer, this is the consultant user id
    private Long consultantId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private Long id;
        private String addressLine;
        private String city;
        private String district;
        private String state;
        private String postalCode;
    }
}

