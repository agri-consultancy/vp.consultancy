package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.*;
import com.example.vp.consultancy.entity.*;
import com.example.vp.consultancy.repository.*;
import com.example.vp.consultancy.util.ExcelScheduleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MasterScheduleService {

    @Autowired
    private MasterScheduleTemplateRepository templateRepository;

    @Autowired
    private CropVarietyRepository cropVarietyRepository;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private MasterScheduleDayRepository dayRepository;

    @Autowired
    private MasterScheduleTaskRepository taskRepository;

    @Autowired
    private ExcelScheduleProcessor excelProcessor;

    // ==================== Template Operations ====================

    @Cacheable(cacheNames = "templatesByConsultant", key = "#consultantId")
    public List<MasterScheduleTemplateDTO> getAllTemplatesByConsultant(Long consultantId) {
        List<MasterScheduleTemplate> templates = templateRepository.findByConsultantId(consultantId);
        return templates.stream().map(this::convertTemplateToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "activeTemplatesByConsultantAndVariety", key = "#consultantId + ':' + #cropVarietyId")
    public List<MasterScheduleTemplateDTO> getActiveTemplatesByConsultantAndCropVariety(
            Long consultantId, Long cropVarietyId) {
        cropVarietyRepository.findByIdAndConsultantId(cropVarietyId, consultantId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Crop variety not found with ID: " + cropVarietyId + " for this consultant"));

        List<MasterScheduleTemplate> templates = templateRepository
            .findByConsultantIdAndCropVarietyIdAndStatusOrderByVersionDesc(consultantId, cropVarietyId, "ACTIVE");

        return templates.stream().map(this::convertTemplateToDTO).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "templateById", key = "#templateId")
    public MasterScheduleTemplateWithDaysDTO getTemplateById(Long templateId) {
        MasterScheduleTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
        return convertTemplateDetailedToDTO(template);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templatesByConsultant", allEntries = true),
            @CacheEvict(cacheNames = "activeTemplatesByConsultantAndVariety", allEntries = true),
            @CacheEvict(cacheNames = "templateById", allEntries = true),
            @CacheEvict(cacheNames = "templateDayById", allEntries = true),
            @CacheEvict(cacheNames = "templateTaskById", allEntries = true),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public TemplateImportResponseDTO importScheduleFromExcel(
            Long consultantId,
            CreateTemplateRequestDTO requestDTO,
            MultipartFile excelFile,
            Boolean createNewVersion) {
        
        try {
            // Parse Excel file
            List<ExcelScheduleProcessor.ScheduleDayData> scheduleDays = excelProcessor.parseScheduleExcel(excelFile);
            
            MasterScheduleTemplate template;
            Long version = requestDTO.getVersion();
            
            // Check if template exists
            boolean templateExists = templateRepository.existsByConsultantIdAndCropVarietyIdAndVersion(
                consultantId, requestDTO.getCropVarietyId(), version);
            
            if (templateExists && (createNewVersion == null || !createNewVersion)) {
                // Update existing template - clear existing days
                template = templateRepository.findByConsultantIdAndCropVarietyIdAndVersion(
                    consultantId, requestDTO.getCropVarietyId(), version);
                template.getScheduleDays().clear();
            } else if (templateExists && createNewVersion) {
                // Create new version
                MasterScheduleTemplate existingTemplate = templateRepository
                    .findByConsultantIdAndCropVarietyIdAndVersion(consultantId, requestDTO.getCropVarietyId(), version);
                version = (existingTemplate != null && existingTemplate.getVersion() != null) 
                    ? existingTemplate.getVersion() + 1 
                    : version + 1;
                template = new MasterScheduleTemplate();
                template.setConsultantId(consultantId);
                template.setVersion(version);
            } else {
                // Create new template
                template = new MasterScheduleTemplate();
                template.setConsultantId(consultantId);
                template.setVersion(version);
            }
            
            template.setCropVarietyId(requestDTO.getCropVarietyId());
            template.setDescription(requestDTO.getDescription());
            template.setStatus(requestDTO.getStatus());
            
            // Save template first
            template = templateRepository.save(template);
            
            int totalDaysImported = 0;
            int totalTasksImported = 0;
            
            // Process each day and its tasks
            for (ExcelScheduleProcessor.ScheduleDayData dayData : scheduleDays) {
                CreateDayRequestDTO dayDTO = dayData.getDay();
                
                MasterScheduleDay day = new MasterScheduleDay();
                day.setTemplate(template);
                day.setDayNumber(dayDTO.getDayNumber());
                day.setTitle(dayDTO.getTitle());
                day.setDescription(dayDTO.getDescription());
                day.setDisplayOrder(dayDTO.getDisplayOrder());
                
                day = dayRepository.save(day);
                totalDaysImported++;
                
                // Save tasks for this day
                for (CreateTaskRequestDTO taskDTO : dayData.getTasks()) {
                    MasterScheduleTask task = new MasterScheduleTask();
                    task.setScheduleDay(day);
                    task.setFertilizerName(taskDTO.getFertilizerName());
                    task.setQuantity(taskDTO.getQuantity());
                    task.setProportion(taskDTO.getProportion());
                    task.setPriority(taskDTO.getPriority());
                    task.setDescription(taskDTO.getDescription());
                    task.setTaskType(taskDTO.getTaskType());
                    
                    taskRepository.save(task);
                    totalTasksImported++;
                }
            }
            
            return TemplateImportResponseDTO.builder()
                .success(true)
                .templateId(template.getId())
                .cropVarietyId(template.getCropVarietyId())
                .version(template.getVersion())
                .totalDaysImported(totalDaysImported)
                .totalTasksImported(totalTasksImported)
                .message("Template created successfully")
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (IOException e) {
            log.error("Error reading Excel file: ", e);
            throw new IllegalArgumentException("Error reading Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error importing schedule: ", e);
            return TemplateImportResponseDTO.builder()
                .success(false)
                .templateId(null)
                .error(e.getMessage())
                .rollbackMessage("Transaction rolled back. No data was imported.")
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templatesByConsultant", allEntries = true),
            @CacheEvict(cacheNames = "activeTemplatesByConsultantAndVariety", allEntries = true),
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", allEntries = true),
            @CacheEvict(cacheNames = "templateTaskById", allEntries = true),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public MasterScheduleTemplateDTO updateTemplate(Long templateId, UpdateTemplateRequestDTO requestDTO) {
        MasterScheduleTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
        
        if (requestDTO.getDescription() != null) {
            template.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getStatus() != null) {
            template.setStatus(requestDTO.getStatus());
        }
        
        template = templateRepository.save(template);
        return convertTemplateToDTO(template);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templatesByConsultant", allEntries = true),
            @CacheEvict(cacheNames = "activeTemplatesByConsultantAndVariety", allEntries = true),
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", allEntries = true),
            @CacheEvict(cacheNames = "templateTaskById", allEntries = true),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public void deleteTemplate(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new IllegalArgumentException("Template not found with ID: " + templateId);
        }
        templateRepository.deleteById(templateId);
    }

    // ==================== Day Operations ====================

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", allEntries = true),
            @CacheEvict(cacheNames = "templateTaskById", allEntries = true),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public MasterScheduleDayDTO addDay(Long templateId, CreateDayRequestDTO requestDTO) {
        MasterScheduleTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
        
        // Check for duplicate day number
        boolean dayExists = dayRepository.existsByTemplateIdAndDayNumber(templateId, requestDTO.getDayNumber());
        if (dayExists) {
            throw new IllegalArgumentException("Day with number " + requestDTO.getDayNumber() + " already exists in this template");
        }
        
        MasterScheduleDay day = new MasterScheduleDay();
        day.setTemplate(template);
        day.setDayNumber(requestDTO.getDayNumber());
        day.setTitle(requestDTO.getTitle());
        day.setDescription(requestDTO.getDescription());
        day.setDisplayOrder(requestDTO.getDisplayOrder());
        
        day = dayRepository.save(day);
        return convertDayToDTO(day);
    }

    @Cacheable(cacheNames = "templateDayById", key = "#templateId + ':' + #dayId")
    public MasterScheduleDayDTO getDayById(Long templateId, Long dayId) {
        MasterScheduleDay day = dayRepository.findById(dayId)
            .orElseThrow(() -> new IllegalArgumentException("Day not found with ID: " + dayId));
        
        if (!day.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("Day does not belong to template with ID: " + templateId);
        }
        
        return convertDayDetailedToDTO(day);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", key = "#templateId + ':' + #dayId"),
            @CacheEvict(cacheNames = "templateTaskById", allEntries = true),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public MasterScheduleDayDTO updateDay(Long templateId, Long dayId, CreateDayRequestDTO requestDTO) {
        MasterScheduleDay day = dayRepository.findById(dayId)
            .orElseThrow(() -> new IllegalArgumentException("Day not found with ID: " + dayId));
        
        if (!day.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("Day does not belong to template with ID: " + templateId);
        }
        
        // Check if day number is being changed and if new day number already exists
        if (!day.getDayNumber().equals(requestDTO.getDayNumber())) {
            boolean dayExists = dayRepository.existsByTemplateIdAndDayNumber(templateId, requestDTO.getDayNumber());
            if (dayExists) {
                throw new IllegalArgumentException("Day with number " + requestDTO.getDayNumber() + " already exists in this template");
            }
        }
        
        day.setDayNumber(requestDTO.getDayNumber());
        day.setTitle(requestDTO.getTitle());
        day.setDescription(requestDTO.getDescription());
        day.setDisplayOrder(requestDTO.getDisplayOrder());
        
        day = dayRepository.save(day);
        return convertDayToDTO(day);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", key = "#templateId + ':' + #dayId"),
            @CacheEvict(cacheNames = "templateTaskById", allEntries = true),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public void deleteDay(Long templateId, Long dayId) {
        MasterScheduleDay day = dayRepository.findById(dayId)
            .orElseThrow(() -> new IllegalArgumentException("Day not found with ID: " + dayId));
        
        if (!day.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("Day does not belong to template with ID: " + templateId);
        }
        
        dayRepository.deleteById(dayId);
    }

    // ==================== Task Operations ====================

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", key = "#templateId + ':' + #dayId"),
            @CacheEvict(cacheNames = "templateTaskById", allEntries = true),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public MasterScheduleTaskDTO addTask(Long templateId, Long dayId, CreateTaskRequestDTO requestDTO) {
        MasterScheduleDay day = dayRepository.findById(dayId)
            .orElseThrow(() -> new IllegalArgumentException("Day not found with ID: " + dayId));
        
        if (!day.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("Day does not belong to template with ID: " + templateId);
        }
        
        MasterScheduleTask task = new MasterScheduleTask();
        task.setScheduleDay(day);
        task.setFertilizerName(requestDTO.getFertilizerName());
        task.setQuantity(requestDTO.getQuantity());
        task.setProportion(requestDTO.getProportion());
        task.setPriority(requestDTO.getPriority());
        task.setDescription(requestDTO.getDescription());
        task.setTaskType(requestDTO.getTaskType());
        
        task = taskRepository.save(task);
        return convertTaskToDTO(task);
    }

    @Cacheable(cacheNames = "templateTaskById", key = "#templateId + ':' + #dayId + ':' + #taskId")
    public MasterScheduleTaskDTO getTaskById(Long templateId, Long dayId, Long taskId) {
        MasterScheduleTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        
        MasterScheduleDay day = task.getScheduleDay();
        if (!day.getId().equals(dayId)) {
            throw new IllegalArgumentException("Task does not belong to day with ID: " + dayId);
        }
        
        if (!day.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("Day does not belong to template with ID: " + templateId);
        }
        
        return convertTaskToDTO(task);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", key = "#templateId + ':' + #dayId"),
            @CacheEvict(cacheNames = "templateTaskById", key = "#templateId + ':' + #dayId + ':' + #taskId"),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public MasterScheduleTaskDTO updateTask(Long templateId, Long dayId, Long taskId, CreateTaskRequestDTO requestDTO) {
        MasterScheduleTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        
        MasterScheduleDay day = task.getScheduleDay();
        if (!day.getId().equals(dayId)) {
            throw new IllegalArgumentException("Task does not belong to day with ID: " + dayId);
        }
        
        if (!day.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("Day does not belong to template with ID: " + templateId);
        }
        
        task.setFertilizerName(requestDTO.getFertilizerName());
        task.setQuantity(requestDTO.getQuantity());
        task.setProportion(requestDTO.getProportion());
        task.setPriority(requestDTO.getPriority());
        task.setDescription(requestDTO.getDescription());
        task.setTaskType(requestDTO.getTaskType());
        
        task = taskRepository.save(task);
        return convertTaskToDTO(task);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "templateById", key = "#templateId"),
            @CacheEvict(cacheNames = "templateDayById", key = "#templateId + ':' + #dayId"),
            @CacheEvict(cacheNames = "templateTaskById", key = "#templateId + ':' + #dayId + ':' + #taskId"),
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true)
    })
    public void deleteTask(Long templateId, Long dayId, Long taskId) {
        MasterScheduleTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        
        MasterScheduleDay day = task.getScheduleDay();
        if (!day.getId().equals(dayId)) {
            throw new IllegalArgumentException("Task does not belong to day with ID: " + dayId);
        }
        
        if (!day.getTemplate().getId().equals(templateId)) {
            throw new IllegalArgumentException("Day does not belong to template with ID: " + templateId);
        }
        
        taskRepository.deleteById(taskId);
    }

    // ==================== Conversion Methods ====================

    private MasterScheduleTemplateDTO convertTemplateToDTO(MasterScheduleTemplate template) {
        CropVariety cropVariety = cropVarietyRepository.findById(template.getCropVarietyId())
                .orElseThrow(() -> new IllegalArgumentException("Crop variety not found for this consultant"));

        return MasterScheduleTemplateDTO.builder()
            .id(template.getId())
            .cropVarietyId(template.getCropVarietyId())
            .cropName(cropVariety.getCrop().getName())
            .cropVarietyName(cropVariety.getName())
            .version(template.getVersion())
            .description(template.getDescription())
            .status(template.getStatus())
            .totalPhases(template.getScheduleDays() != null ? template.getScheduleDays().size() : 0)
            .updatedAt(template.getUpdatedAt())
            .createdAt(template.getCreatedAt())
            .build();
    }

    private MasterScheduleTemplateWithDaysDTO convertTemplateDetailedToDTO(MasterScheduleTemplate template) {
        CropVariety cropVariety = cropVarietyRepository.findById(template.getCropVarietyId())
                .orElseThrow(() -> new IllegalArgumentException("Crop variety not found for this consultant"));

        return MasterScheduleTemplateWithDaysDTO.builder()
            .id(template.getId())
            .cropVarietyId(template.getCropVarietyId())
            .cropVarietyName(cropVariety.getName())
            .version(template.getVersion())
            .description(template.getDescription())
            .status(template.getStatus())
            .totalPhases(template.getScheduleDays() != null ? template.getScheduleDays().size() : 0)
            .scheduleDays(template.getScheduleDays() != null
                ? template.getScheduleDays().stream()
                    .map(this::convertDayDetailedToDTO)
                    .collect(Collectors.toList())
                : List.of())
            .build();
    }

    private MasterScheduleDayDTO convertDayToDTO(MasterScheduleDay day) {
        return MasterScheduleDayDTO.builder()
            .id(day.getId())
            .templateId(day.getTemplate().getId())
            .dayNumber(day.getDayNumber())
            .title(day.getTitle())
            .description(day.getDescription())
            .displayOrder(day.getDisplayOrder())
            .createdAt(day.getCreatedAt())
            .updatedAt(day.getUpdatedAt())
            .build();
    }

    private MasterScheduleDayDTO convertDayDetailedToDTO(MasterScheduleDay day) {
        MasterScheduleDayDTO dto = convertDayToDTO(day);
        
        if (day.getTasks() != null) {
            dto.setTasks(day.getTasks().stream()
                .map(this::convertTaskToDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private MasterScheduleTaskDTO convertTaskToDTO(MasterScheduleTask task) {
        return MasterScheduleTaskDTO.builder()
            .id(task.getId())
            .scheduleDayId(task.getScheduleDay().getId())
            .fertilizerName(task.getFertilizerName())
            .quantity(task.getQuantity())
            .proportion(task.getProportion())
            .priority(task.getPriority())
            .description(task.getDescription())
            .taskType(task.getTaskType())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
