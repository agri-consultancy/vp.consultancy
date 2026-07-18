package com.example.vp.consultancy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consultant Advertisement Response DTO - Output when returning advertisement details
 * Returned by APIs when creating, retrieving, or updating advertisements
 * @author VP Consultancy Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantAdvertisementResponse {

    private Long id;

    private String url;

    private String title;

    private String descriptions;

    private Long priority;

    private String type;

    @JsonProperty("consultantId")
    private Long consultantId;

    @JsonProperty("consultantName")
    private String consultantName;

    private String createdAt;

    private String updatedAt;
}
