package com.example.vp.consultancy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterScheduleTemplateWithDaysDTO {
    private Long id;
    private Long cropVarietyId;
    private String cropVarietyName;
    private Long version;
    private String description;
    private String status;
    private Integer totalPhases;

    private List<MasterScheduleDayDTO> scheduleDays;

}
