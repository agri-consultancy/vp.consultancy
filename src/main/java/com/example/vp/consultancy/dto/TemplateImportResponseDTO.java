package com.example.vp.consultancy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateImportResponseDTO {
    private Boolean success;
    private Long templateId;
    private Long cropVarietyId;
    private Long version;
    private Integer totalDaysImported;
    private Integer totalTasksImported;
    private String message;
    private String error;
    private String failedDayNumber;
    private String rollbackMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
