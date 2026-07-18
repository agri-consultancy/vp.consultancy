package com.example.vp.consultancy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MasterScheduleTemplateDTO {
    private Long id;
    private Long cropVarietyId;
    private String cropName;
    private String cropVarietyName;
    private Long version;
    private String description;
    private String status;
    private Integer totalPhases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
