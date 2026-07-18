package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.dto.FarmerScheduleResponse;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewRequest;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewResponse;
import com.example.vp.consultancy.dto.ScheduleDayDTO;
import com.example.vp.consultancy.dto.ScheduleTaskDTO;
import com.example.vp.consultancy.dto.SendScheduleDayRequest;
import com.example.vp.consultancy.dto.SendScheduleRequest;
import com.example.vp.consultancy.dto.SendScheduleResponse;
import com.example.vp.consultancy.dto.SendScheduleTaskRequest;
import com.example.vp.consultancy.entity.FarmerCropVariety;
import com.example.vp.consultancy.entity.FarmerCropVarietySchedule;
import com.example.vp.consultancy.entity.FarmerScheduleDay;
import com.example.vp.consultancy.entity.FarmerScheduleTask;
import com.example.vp.consultancy.entity.MasterScheduleDay;
import com.example.vp.consultancy.entity.MasterScheduleTask;
import com.example.vp.consultancy.entity.MasterScheduleTemplate;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.exception.VPException;
import com.example.vp.consultancy.repository.FarmerCropVarietyRepository;
import com.example.vp.consultancy.repository.FarmerCropVarietyScheduleRepository;
import com.example.vp.consultancy.repository.MasterScheduleDayRepository;
import com.example.vp.consultancy.repository.MasterScheduleTemplateRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.service.SendScheduleService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SendScheduleServiceImpl implements SendScheduleService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SendScheduleServiceImpl.class);
    private final UserProfileRepository userProfileRepository;
    private final FarmerCropVarietyRepository farmerCropVarietyRepository;
    private final MasterScheduleTemplateRepository masterScheduleTemplateRepository;
    private final MasterScheduleDayRepository masterScheduleDayRepository;
    private final FarmerCropVarietyScheduleRepository farmerCropVarietyScheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public GetNextSchedulePreviewResponse getNextSchedulePreview(GetNextSchedulePreviewRequest request) {
        logger.info("Fetching next schedule preview for Farmer ID: {}, FarmerCropVariety ID: {}, MasterScheduleTemplate ID: {}, Number of Days: {}",
                request.getFarmerId(), request.getFarmerCropVarietyId(), request.getMasterScheduleTemplateId(), request.getNumberOfDays());
        UserProfile farmer = userProfileRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + request.getFarmerId()));

        FarmerCropVariety farmerCropVariety = validateFarmerCropVarietyOwnership(farmer.getId(), request.getFarmerCropVarietyId());
        MasterScheduleTemplate template = masterScheduleTemplateRepository.findById(request.getMasterScheduleTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Master schedule template not found with ID: " + request.getMasterScheduleTemplateId()));

        if (!Objects.equals(template.getCropVarietyId(), farmerCropVariety.getCropVariety().getId())) {
            throw new VPException("Selected master schedule template does not belong to the farmer crop variety", 400);
        }

        Long lastSentDay = farmerCropVarietyScheduleRepository
                .findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(request.getFarmerId(), request.getFarmerCropVarietyId())
                .map(FarmerCropVarietySchedule::getLastSentDay)
                .orElse(0L);

        long startDay = lastSentDay + 1;
        long endDay = lastSentDay + request.getNumberOfDays();

        List<MasterScheduleDay> scheduleDays = masterScheduleDayRepository
                .findByTemplateIdAndDayNumberBetweenOrderByDayNumberAsc(request.getMasterScheduleTemplateId(), startDay, endDay);

        List<ScheduleDayDTO> dayDTOs = scheduleDays.stream()
                .map(this::convertMasterDayToScheduleDay)
                .collect(Collectors.toList());

        return GetNextSchedulePreviewResponse.builder()
                .farmerId(request.getFarmerId())
                .farmerCropVarietyId(request.getFarmerCropVarietyId())
                .masterScheduleTemplateId(request.getMasterScheduleTemplateId())
                .startDay(startDay)
                .endDay(endDay)
                .totalDays((long) dayDTOs.size())
                .scheduleDays(dayDTOs)
                .build();
    }

    @Override
    @Transactional
    public SendScheduleResponse sendSchedule(SendScheduleRequest request) {
        UserProfile farmer = userProfileRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + request.getFarmerId()));
        FarmerCropVariety farmerCropVariety = validateFarmerCropVarietyOwnership(farmer.getId(), request.getFarmerCropVarietyId());

        List<SendScheduleDayRequest> sortedDays = request.getScheduleDays().stream()
                .sorted(Comparator.comparing(SendScheduleDayRequest::getDayNumber))
                .collect(Collectors.toList());

        long startDay = sortedDays.get(0).getDayNumber();
        long endDay = sortedDays.get(sortedDays.size() - 1).getDayNumber();

        FarmerCropVarietySchedule schedule = FarmerCropVarietySchedule.builder()
                .farmer(farmer)
                .farmerCropVariety(farmerCropVariety)
                .startDate(LocalDate.now())
                .lastSentDay(endDay)
                .build();

        List<FarmerScheduleDay> farmerScheduleDays = sortedDays.stream()
                .map(dayRequest -> convertRequestToFarmerDay(dayRequest, schedule, farmer, farmerCropVariety))
                .collect(Collectors.toList());

        schedule.setScheduleDays(farmerScheduleDays);
        FarmerCropVarietySchedule savedSchedule = farmerCropVarietyScheduleRepository.save(schedule);

        return SendScheduleResponse.builder()
                .farmerId(request.getFarmerId())
                .varietyId(request.getFarmerCropVarietyId())
                .scheduleId(savedSchedule.getId())
                .daysSent(request.getNumberOfDays())
                .startDay(startDay)
                .endDay(endDay)
                .message("Schedule sent successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FarmerScheduleResponse getFarmerSchedule(UserProfile farmer, Long farmerCropVarietyId) {
        FarmerCropVariety farmerCropVariety = validateFarmerCropVarietyOwnership(farmer.getId(), farmerCropVarietyId);

        List<FarmerCropVarietySchedule> schedules = farmerCropVarietyScheduleRepository
                .findByFarmerCropVarietyIdOrderByIdAsc(farmerCropVarietyId);

        List<FarmerScheduleResponse.AssignedScheduleDTO> assignedSchedules = schedules.stream()
                .map(this::convertToAssignedSchedule)
                .collect(Collectors.toList());

        String mobile = farmer.getUser() != null ? farmer.getUser().getMobile() : null;
        String cropName = farmerCropVariety.getCropVariety() != null && farmerCropVariety.getCropVariety().getCrop() != null
                ? farmerCropVariety.getCropVariety().getCrop().getName() : null;
        String cropVarietyName = farmerCropVariety.getCropVariety() != null
                ? farmerCropVariety.getCropVariety().getName() : null;

        return FarmerScheduleResponse.builder()
                .farmer(FarmerScheduleResponse.FarmerDetails.builder()
                        .farmerId(farmer.getId())
                        .farmerName(farmer.getFullName())
                        .mobile(mobile)
                        .email(farmer.getEmail())
                        .build())
                .cropVariety(FarmerScheduleResponse.CropVarietyDetails.builder()
                        .farmerCropVarietyId(farmerCropVariety.getId())
                        .cropVarietyId(farmerCropVariety.getCropVariety() != null ? farmerCropVariety.getCropVariety().getId() : null)
                        .cropName(cropName)
                        .cropVarietyName(cropVarietyName)
                        .status(farmerCropVariety.getStatus())
                        .sowingDate(farmerCropVariety.getSowingDate())
                        .expectedHarvestDate(farmerCropVariety.getExpectedHarvestDate())
                        .build())
                .schedules(assignedSchedules)
                .build();
    }

    private UserProfile getCurrentFarmerProfile() {
        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        return userProfileRepository.findByUser_Mobile(mobile)
                .orElseThrow(() -> {
                    logger.warn("Farmer profile not found for authenticated user with mobile: {}", mobile);
                    return new ResourceNotFoundException("Farmer profile not found for authenticated user");
                });
    }

    private FarmerCropVariety validateFarmerCropVarietyOwnership(Long farmerId, Long farmerCropVarietyId) {
        logger.info("Validating ownership of FarmerCropVariety with ID: {} for Farmer with ID: {}", farmerCropVarietyId, farmerId);
        FarmerCropVariety farmerCropVariety = farmerCropVarietyRepository.findById(farmerCropVarietyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Farmer crop variety not found with ID: " + farmerCropVarietyId));

        if (!Objects.equals(farmerCropVariety.getFarmer().getId(), farmerId)) {
            logger.warn("Farmer crop variety with ID: {} does not belong to farmer ID: {}", farmerCropVarietyId, farmerId);
            throw new ResourceNotFoundException("Farmer crop variety does not belong to farmer ID: " + farmerId);
        }

        return farmerCropVariety;
    }

    private ScheduleDayDTO convertMasterDayToScheduleDay(MasterScheduleDay day) {
        List<ScheduleTaskDTO> taskDTOs = day.getTasks() == null ? List.of() : day.getTasks().stream()
                .sorted(Comparator.comparing(MasterScheduleTask::getPriority, Comparator.nullsLast(Long::compareTo)))
                .map(task -> ScheduleTaskDTO.builder()
                        .taskId(task.getId())
                        .priority(task.getPriority())
                        .taskType(task.getTaskType())
                        .taskDescription(task.getDescription())
                        .fertilizerName(task.getFertilizerName())
                        .quantity(task.getQuantity())
                        .proportion(task.getProportion())
                        .build())
                .collect(Collectors.toList());

        return ScheduleDayDTO.builder()
                .dayNumber(day.getDayNumber())
                .dayTitle(day.getTitle())
                .dayDescription(day.getDescription())
                .tasks(taskDTOs)
                .build();
    }

    private FarmerScheduleDay convertRequestToFarmerDay(SendScheduleDayRequest request, FarmerCropVarietySchedule schedule,
                                                        UserProfile farmer, FarmerCropVariety farmerCropVariety) {
        FarmerScheduleDay day = FarmerScheduleDay.builder()
                .schedule(schedule)
                .farmer(farmer)
                .farmerCropVariety(farmerCropVariety)
                .dayNumber(request.getDayNumber())
                .title(request.getDayTitle())
                .description(request.getDayDescription())
                .status(request.getStatus())
                .displayOrder(request.getDisplayOrder())
                .build();

        List<FarmerScheduleTask> tasks = request.getTasks().stream()
                .map(taskRequest -> convertRequestToFarmerTask(taskRequest, day))
                .collect(Collectors.toList());
        day.setTasks(tasks);
        return day;
    }

    private FarmerScheduleTask convertRequestToFarmerTask(SendScheduleTaskRequest request, FarmerScheduleDay day) {
        return FarmerScheduleTask.builder()
                .scheduleDay(day)
                .fertilizerName(StringUtils.hasText(request.getFertilizerName()) ? request.getFertilizerName() : "N/A")
                .quantity(request.getQuantity())
                .proportion(request.getProportion())
                .priority(request.getPriority())
                .description(request.getTaskDescription())
                .taskType(request.getTaskType())
                .build();
    }

    private FarmerScheduleResponse.AssignedScheduleDTO convertToAssignedSchedule(FarmerCropVarietySchedule schedule) {
        List<FarmerScheduleDay> sortedDays = schedule.getScheduleDays() == null ? List.of() : schedule.getScheduleDays().stream()
                .sorted(Comparator.comparing(FarmerScheduleDay::getDayNumber))
                .collect(Collectors.toList());

        List<ScheduleDayDTO> scheduleDays = sortedDays.stream()
                .map(this::convertFarmerDayToScheduleDay)
                .collect(Collectors.toList());

        long startDay = sortedDays.isEmpty() ? 0L : sortedDays.get(0).getDayNumber();
        long endDay = sortedDays.isEmpty() ? 0L : sortedDays.get(sortedDays.size() - 1).getDayNumber();

        return FarmerScheduleResponse.AssignedScheduleDTO.builder()
                .scheduleId(schedule.getId())
                .startDate(schedule.getStartDate())
                .startDay(startDay)
                .endDay(endDay)
                .daysSent((long) sortedDays.size())
                .scheduleDays(scheduleDays)
                .build();
    }

    private ScheduleDayDTO convertFarmerDayToScheduleDay(FarmerScheduleDay day) {
        List<ScheduleTaskDTO> tasks = day.getTasks() == null ? List.of() : day.getTasks().stream()
                .sorted(Comparator.comparing(FarmerScheduleTask::getPriority, Comparator.nullsLast(Long::compareTo)))
                .map(task -> ScheduleTaskDTO.builder()
                        .taskType(task.getTaskType())
                        .taskDescription(task.getDescription())
                        .fertilizerName(task.getFertilizerName())
                        .quantity(task.getQuantity())
                        .proportion(task.getProportion())
                        .priority(task.getPriority())
                        .taskId(task.getId())
                        .build())
                .collect(Collectors.toList());

        return ScheduleDayDTO.builder()
                .dayNumber(day.getDayNumber())
                .dayTitle(day.getTitle())
                .dayDescription(day.getDescription())
                .tasks(tasks)
                .build();
    }
}
