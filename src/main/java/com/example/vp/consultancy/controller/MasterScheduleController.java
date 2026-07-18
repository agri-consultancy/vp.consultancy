package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.dto.*;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.service.MasterScheduleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for consultant operations on Master Schedules.
 *
 * Provides endpoints for:
 * - Create master schedule from Excel import
 * - Update/Delete master schedule templates
 * - Manage schedule days and tasks
 * - Full CRUD operations on master schedules
 *
 * All endpoints are secured with @PreAuthorize("hasRole('CONSULTANT')")
 * and require valid JWT authentication.
 */

@Slf4j
@RestController
@RequestMapping("/api/consultant/schedule")
@PreAuthorize("hasRole('CONSULTANT')")
public class MasterScheduleController {

    @Autowired
    private MasterScheduleService masterScheduleService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Helper method to extract consultant ID from authentication
    private Long getConsultantIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Invalid authentication context");
        }

        String consultantMobile = authentication.getName();
        UserProfile consultantProfile = userProfileRepository.findByUser_Mobile(consultantMobile)
                .orElseThrow(() -> new IllegalArgumentException("Consultant profile not found"));

        return consultantProfile.getId();
    }

    // ==================== TEMPLATE ENDPOINTS ====================

    /**
     * API 1: List all templates for the consultant
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponseDTO<List<MasterScheduleTemplateDTO>>> getAllTemplates(Authentication authentication) {
        try {
            Long consultantId = getConsultantIdFromAuth(authentication);
            List<MasterScheduleTemplateDTO> templates = masterScheduleService.getAllTemplatesByConsultant(consultantId);
            
            ApiResponseDTO<List<MasterScheduleTemplateDTO>> response = ApiResponseDTO.<List<MasterScheduleTemplateDTO>>builder()
                .success(true)
                .data(templates)
                .totalCount(templates.size())
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching templates: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<List<MasterScheduleTemplateDTO>>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 2: Import template from Excel file
     */
    @PostMapping("/import-excel")
    public ResponseEntity<TemplateImportResponseDTO> importTemplateFromExcel(
            @Valid @RequestParam("crop_variety_id") Long cropVarietyId,
            @Valid @RequestParam("version") Long version,
            @RequestParam(value = "description", required = false) String description,
            @Valid @RequestParam("status") String status,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "create_new_version", required = false) Boolean createNewVersion,
            Authentication authentication) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(TemplateImportResponseDTO.builder()
                        .success(false)
                        .error("File is required")
                        .timestamp(LocalDateTime.now())
                        .build());
            }
            
            if (!file.getContentType().contains("spreadsheet") && !file.getOriginalFilename().endsWith(".xlsx")) {
                return ResponseEntity.badRequest()
                    .body(TemplateImportResponseDTO.builder()
                        .success(false)
                        .error("Only .xlsx files are supported")
                        .timestamp(LocalDateTime.now())
                        .build());
            }
            
            Long consultantId = getConsultantIdFromAuth(authentication);
            
            CreateTemplateRequestDTO requestDTO = CreateTemplateRequestDTO.builder()
                .cropVarietyId(cropVarietyId)
                .version(version)
                .description(description)
                .status(status)
                .build();
            
            TemplateImportResponseDTO response = masterScheduleService.importScheduleFromExcel(
                consultantId, requestDTO, file, createNewVersion);
            
            if (response.getSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error importing template: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TemplateImportResponseDTO.builder()
                    .success(false)
                    .error(e.getMessage())
                    .rollbackMessage("Transaction rolled back. No data was imported.")
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 3: Get template details with all days and tasks
     */
    @GetMapping("/templates/{template_id}")
    public ResponseEntity<ApiResponseDTO<MasterScheduleTemplateWithDaysDTO>> getTemplateDetails(
            @PathVariable("template_id") Long templateId) {
        try {
            MasterScheduleTemplateWithDaysDTO template = masterScheduleService.getTemplateById(templateId);
            
            ApiResponseDTO<MasterScheduleTemplateWithDaysDTO> response = ApiResponseDTO.<MasterScheduleTemplateWithDaysDTO>builder()
                .success(true)
                .data(template)
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.<MasterScheduleTemplateWithDaysDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error fetching template: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleTemplateWithDaysDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 4: Update template metadata
     */
    @PutMapping("/templates/{template_id}")
    public ResponseEntity<ApiResponseDTO<MasterScheduleTemplateDTO>> updateTemplate(
            @PathVariable("template_id") Long templateId,
            @RequestBody UpdateTemplateRequestDTO requestDTO) {
        try {
            MasterScheduleTemplateDTO updatedTemplate = masterScheduleService.updateTemplate(templateId, requestDTO);
            
            ApiResponseDTO<MasterScheduleTemplateDTO> response = ApiResponseDTO.<MasterScheduleTemplateDTO>builder()
                .success(true)
                .data(updatedTemplate)
                .message("Template updated successfully")
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.<MasterScheduleTemplateDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error updating template: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleTemplateDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 5: Delete template
     */
    @DeleteMapping("/templates/{template_id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTemplate(
            @PathVariable("template_id") Long templateId) {
        try {
            masterScheduleService.deleteTemplate(templateId);
            
            ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Template deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.<Void>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error deleting template: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<Void>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    // ==================== DAY ENDPOINTS ====================

    /**
     * API 6: Add new day to template
     */
    @PostMapping("/templates/{template_id}/days")
    public ResponseEntity<ApiResponseDTO<MasterScheduleDayDTO>> addDay(
            @PathVariable("template_id") Long templateId,
            @Valid @RequestBody CreateDayRequestDTO requestDTO) {
        try {
            MasterScheduleDayDTO newDay = masterScheduleService.addDay(templateId, requestDTO);
            
            ApiResponseDTO<MasterScheduleDayDTO> response = ApiResponseDTO.<MasterScheduleDayDTO>builder()
                .success(true)
                .data(newDay)
                .message("Day added successfully")
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.<MasterScheduleDayDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error adding day: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleDayDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 7: Get day details with tasks
     */
    @GetMapping("/templates/{template_id}/days/{day_id}")
    public ResponseEntity<ApiResponseDTO<MasterScheduleDayDTO>> getDay(
            @PathVariable("template_id") Long templateId,
            @PathVariable("day_id") Long dayId) {
        try {
            MasterScheduleDayDTO day = masterScheduleService.getDayById(templateId, dayId);
            
            ApiResponseDTO<MasterScheduleDayDTO> response = ApiResponseDTO.<MasterScheduleDayDTO>builder()
                .success(true)
                .data(day)
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.<MasterScheduleDayDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error fetching day: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleDayDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 8: Update day details
     */
    @PutMapping("/templates/{template_id}/days/{day_id}")
    public ResponseEntity<ApiResponseDTO<MasterScheduleDayDTO>> updateDay(
            @PathVariable("template_id") Long templateId,
            @PathVariable("day_id") Long dayId,
            @Valid @RequestBody CreateDayRequestDTO requestDTO) {
        try {
            MasterScheduleDayDTO updatedDay = masterScheduleService.updateDay(templateId, dayId, requestDTO);
            
            ApiResponseDTO<MasterScheduleDayDTO> response = ApiResponseDTO.<MasterScheduleDayDTO>builder()
                .success(true)
                .data(updatedDay)
                .message("Day updated successfully")
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.<MasterScheduleDayDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error updating day: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleDayDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 9: Delete day
     */
    @DeleteMapping("/templates/{template_id}/days/{day_id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDay(
            @PathVariable("template_id") Long templateId,
            @PathVariable("day_id") Long dayId) {
        try {
            masterScheduleService.deleteDay(templateId, dayId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.<Void>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error deleting day: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<Void>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    // ==================== TASK ENDPOINTS ====================

    /**
     * API 10: Add task to a day
     */
    @PostMapping("/templates/{template_id}/days/{day_id}/tasks")
    public ResponseEntity<ApiResponseDTO<MasterScheduleTaskDTO>> addTask(
            @PathVariable("template_id") Long templateId,
            @PathVariable("day_id") Long dayId,
            @Valid @RequestBody CreateTaskRequestDTO requestDTO) {
        try {
            MasterScheduleTaskDTO newTask = masterScheduleService.addTask(templateId, dayId, requestDTO);
            
            ApiResponseDTO<MasterScheduleTaskDTO> response = ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                .success(true)
                .data(newTask)
                .message("Task added successfully")
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error adding task: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 11: Update task
     */
    @PutMapping("/templates/{template_id}/days/{day_id}/tasks/{task_id}")
    public ResponseEntity<ApiResponseDTO<MasterScheduleTaskDTO>> updateTask(
            @PathVariable("template_id") Long templateId,
            @PathVariable("day_id") Long dayId,
            @PathVariable("task_id") Long taskId,
            @Valid @RequestBody CreateTaskRequestDTO requestDTO) {
        try {
            MasterScheduleTaskDTO updatedTask = masterScheduleService.updateTask(templateId, dayId, taskId, requestDTO);
            
            ApiResponseDTO<MasterScheduleTaskDTO> response = ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                .success(true)
                .data(updatedTask)
                .message("Task updated successfully")
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error updating task: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 12: Delete task
     */
    @DeleteMapping("/templates/{template_id}/days/{day_id}/tasks/{task_id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTask(
            @PathVariable("template_id") Long templateId,
            @PathVariable("day_id") Long dayId,
            @PathVariable("task_id") Long taskId) {
        try {
            masterScheduleService.deleteTask(templateId, dayId, taskId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.<Void>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error deleting task: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<Void>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * API 13: Get task details
     */
    @GetMapping("/templates/{template_id}/days/{day_id}/tasks/{task_id}")
    public ResponseEntity<ApiResponseDTO<MasterScheduleTaskDTO>> getTask(
            @PathVariable("template_id") Long templateId,
            @PathVariable("day_id") Long dayId,
            @PathVariable("task_id") Long taskId) {
        try {
            MasterScheduleTaskDTO task = masterScheduleService.getTaskById(templateId, dayId, taskId);
            
            ApiResponseDTO<MasterScheduleTaskDTO> response = ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                .success(true)
                .data(task)
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error fetching task: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<MasterScheduleTaskDTO>builder()
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}
