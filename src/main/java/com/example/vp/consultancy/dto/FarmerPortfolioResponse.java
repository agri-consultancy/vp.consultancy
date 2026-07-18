package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * Farmer Portfolio Response DTO - Displays farmer summary in portfolio list
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerPortfolioResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String mobile;

    private String location;

    private String primarySector;

    private List<String> crops;

    private String nextVisitDate;

    private String visitStatus;

    private String alertMessage;

    private String profileImageUrl;

    private String createdAt;

    private String updatedAt;
}

