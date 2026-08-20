package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.dto.AddScheduleGapRequest;
import com.example.vp.consultancy.dto.AddScheduleGapResponse;
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
import com.example.vp.consultancy.entity.FarmerScheduleGap;
import com.example.vp.consultancy.entity.FarmerScheduleTask;
import com.example.vp.consultancy.entity.MasterScheduleDay;
import com.example.vp.consultancy.entity.MasterScheduleTask;
import com.example.vp.consultancy.entity.MasterScheduleTemplate;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.exception.VPException;
import com.example.vp.consultancy.repository.FarmerCropVarietyRepository;
import com.example.vp.consultancy.repository.FarmerCropVarietyScheduleRepository;
import com.example.vp.consultancy.repository.FarmerScheduleGapRepository;
import com.example.vp.consultancy.repository.MasterScheduleDayRepository;
import com.example.vp.consultancy.repository.MasterScheduleTemplateRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.service.SendScheduleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final FarmerScheduleGapRepository farmerScheduleGapRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "schedulePreview",
            key = "#request.farmerId + ':' + #request.farmerCropVarietyId + ':' + #request.masterScheduleTemplateId + ':' + #request.numberOfDays"
    )
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
            logger.error("Master schedule template with ID: {} does not belong to the crop variety of FarmerCropVariety ID: {}",
                    request.getMasterScheduleTemplateId(), request.getFarmerCropVarietyId());
            throw new VPException("Selected master schedule template does not belong to the farmer crop variety", 400);
        }

        Optional<FarmerCropVarietySchedule> latestSchedule = getLatestSchedule(request.getFarmerId(), request.getFarmerCropVarietyId());
        long lastSentMasterDay = latestSchedule.map(this::resolveLastSentMasterDay).orElse(0L);
        long totalGapDays = getTotalGapDays(request.getFarmerId(), request.getFarmerCropVarietyId());

        long masterStartDay = lastSentMasterDay + 1;
        long masterEndDay = lastSentMasterDay + request.getNumberOfDays();
        long startDay = masterStartDay + totalGapDays;
        long endDay = masterEndDay + totalGapDays;
        logger.info("Calculated schedule preview range: farmerStartDay = {}, farmerEndDay = {}, masterStartDay = {}, masterEndDay = {}, totalGapDays = {}",
                startDay, endDay, masterStartDay, masterEndDay, totalGapDays);

        List<MasterScheduleDay> scheduleDays = masterScheduleDayRepository
                .findByTemplateIdAndDayNumberBetweenOrderByDayNumberAsc(request.getMasterScheduleTemplateId(), masterStartDay, masterEndDay);
        logger.debug("Fetched {} schedule days from MasterScheduleDayRepository for template ID: {} between days {} and {}",
                scheduleDays.size(), request.getMasterScheduleTemplateId(), masterStartDay, masterEndDay);

        List<ScheduleDayDTO> dayDTOs = scheduleDays.stream()
                .map(day -> convertMasterDayToScheduleDay(day, day.getDayNumber() + totalGapDays))
                .collect(Collectors.toList());
        logger.debug("Converted MasterScheduleDay entities to ScheduleDayDTOs, resulting in {} DTOs", dayDTOs.size());

        logger.info("Returning schedule preview response for Farmer ID: {}, FarmerCropVariety ID: {}, MasterScheduleTemplate ID: {}",
                request.getFarmerId(), request.getFarmerCropVarietyId(), request.getMasterScheduleTemplateId());
        return GetNextSchedulePreviewResponse.builder()
                .farmerId(request.getFarmerId())
                .farmerCropVarietyId(request.getFarmerCropVarietyId())
                .masterScheduleTemplateId(request.getMasterScheduleTemplateId())
                .startDay(startDay)
                .endDay(endDay)
                .masterStartDay(masterStartDay)
                .masterEndDay(masterEndDay)
                .totalDays((long) dayDTOs.size())
                .scheduleDays(dayDTOs)
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true),
            @CacheEvict(cacheNames = "farmerScheduleByVariety", allEntries = true)
    })
    public SendScheduleResponse sendSchedule(SendScheduleRequest request) {
        logger.info("Sending schedule for Farmer ID: {}, FarmerCropVariety ID: {}, Number of Days: {}",
                request.getFarmerId(), request.getFarmerCropVarietyId(), request.getNumberOfDays());

        UserProfile farmer = userProfileRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + request.getFarmerId()));
        FarmerCropVariety farmerCropVariety = validateFarmerCropVarietyOwnership(farmer.getId(), request.getFarmerCropVarietyId());
        logger.info("Validated ownership of FarmerCropVariety ID: {} for Farmer ID: {}", request.getFarmerCropVarietyId(), request.getFarmerId());

        Optional<FarmerCropVarietySchedule> latestSchedule = getLatestSchedule(request.getFarmerId(), request.getFarmerCropVarietyId());
        long lastSentMasterDay = latestSchedule.map(this::resolveLastSentMasterDay).orElse(0L);
        long totalGapDays = getTotalGapDays(request.getFarmerId(), request.getFarmerCropVarietyId());
        logger.info("lastSentMasterDay ={}, totalGapDays={}, farmerId = {}",lastSentMasterDay,totalGapDays, request.getFarmerId());

        List<SendScheduleDayRequest> sortedDays = request.getScheduleDays().stream()
                .sorted(Comparator.comparing(SendScheduleDayRequest::getDayNumber))
                .toList();
        logger.info("Sorted {} schedule days by day number for sending schedule", sortedDays.size());

        long expectedStartDay = lastSentMasterDay + totalGapDays + 1;
        validateSendScheduleRequest(request, sortedDays, expectedStartDay);

        long startDay = sortedDays.getFirst().getDayNumber();
        long endDay = sortedDays.getLast().getDayNumber();
        long endMasterDay = lastSentMasterDay + sortedDays.size();
        logger.info("Calculated schedule range for sending: farmerStartDay = {}, farmerEndDay = {}, masterEndDay = {}, totalGapDays = {}",
                startDay, endDay, endMasterDay, totalGapDays);

        FarmerCropVarietySchedule schedule = FarmerCropVarietySchedule.builder()
                .farmer(farmer)
                .farmerCropVariety(farmerCropVariety)
                .startDate(LocalDate.now())
                .lastSentDay(endDay)
                .lastSentMasterDay(endMasterDay)
                .build();

        List<FarmerScheduleDay> farmerScheduleDays = sortedDays.stream()
                .map(dayRequest -> convertRequestToFarmerDay(dayRequest, schedule, farmer, farmerCropVariety))
                .collect(Collectors.toList());
        logger.info("Converted {} SendScheduleDayRequest to FarmerScheduleDay entities", farmerScheduleDays.size());

        schedule.setScheduleDays(farmerScheduleDays);
        FarmerCropVarietySchedule savedSchedule = farmerCropVarietyScheduleRepository.save(schedule);
        logger.info("Saved FarmerCropVarietySchedule with ID: {}", savedSchedule.getId());

        logger.info("Schedule sent successfully for Farmer ID: {}, FarmerCropVariety ID: {}, Schedule ID: {}, Days Sent: {}",
                request.getFarmerId(), request.getFarmerCropVarietyId(), savedSchedule.getId(), request.getNumberOfDays());
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
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "schedulePreview", allEntries = true),
            @CacheEvict(cacheNames = "farmerScheduleByVariety", allEntries = true)
    })
    public AddScheduleGapResponse addScheduleGap(AddScheduleGapRequest request) {
        logger.info("Adding schedule gap for Farmer ID: {}, FarmerCropVariety ID: {}, Gap Days: {}",
                request.getFarmerId(), request.getFarmerCropVarietyId(), request.getGapDays());

        UserProfile farmer = userProfileRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + request.getFarmerId()));
        FarmerCropVariety farmerCropVariety = validateFarmerCropVarietyOwnership(farmer.getId(), request.getFarmerCropVarietyId());

        FarmerCropVarietySchedule latestSchedule = getLatestSchedule(request.getFarmerId(), request.getFarmerCropVarietyId())
                .orElseThrow(() -> new VPException("Cannot add a schedule gap before sending at least one schedule", 400));

        long totalGapDaysBefore = getTotalGapDays(request.getFarmerId(), request.getFarmerCropVarietyId());
        FarmerScheduleGap savedGap = farmerScheduleGapRepository.save(FarmerScheduleGap.builder()
                .farmer(farmer)
                .farmerCropVariety(farmerCropVariety)
                .gapDays(request.getGapDays())
                .build());
        long totalGapDaysAfter = totalGapDaysBefore + savedGap.getGapDays();
        long lastSentMasterDay = resolveLastSentMasterDay(latestSchedule);
        long nextMasterDay = lastSentMasterDay + 1;
        long nextFarmerDay = nextMasterDay + totalGapDaysAfter;

        logger.info("Schedule gap added successfully for Farmer ID: {}, FarmerCropVariety ID: {}, Total Gap Days: {}, Next Farmer Day: {}, Next Master Day: {}",
                request.getFarmerId(), request.getFarmerCropVarietyId(), totalGapDaysAfter, nextFarmerDay, nextMasterDay);

        return AddScheduleGapResponse.builder()
                .farmerId(request.getFarmerId())
                .farmerCropVarietyId(request.getFarmerCropVarietyId())
                .gapDays(savedGap.getGapDays())
                .totalGapDays(totalGapDaysAfter)
                .lastSentFarmerDay(latestSchedule.getLastSentDay())
                .lastSentMasterDay(lastSentMasterDay)
                .nextFarmerDay(nextFarmerDay)
                .nextMasterDay(nextMasterDay)
                .message("Schedule gap added successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "farmerScheduleByVariety", key = "#farmer.id + ':' + #farmerCropVarietyId")
    public FarmerScheduleResponse getFarmerSchedule(UserProfile farmer, Long farmerCropVarietyId) {
        logger.info("Fetching schedule for Farmer ID: {}, FarmerCropVariety ID: {}", farmer.getId(), farmerCropVarietyId);

        FarmerCropVariety farmerCropVariety = validateFarmerCropVarietyOwnership(farmer.getId(), farmerCropVarietyId);

        List<FarmerCropVarietySchedule> schedules = farmerCropVarietyScheduleRepository
                .findByFarmerCropVarietyIdOrderByIdAsc(farmerCropVarietyId);
        logger.info("Fetched {} assigned schedules for FarmerCropVariety ID: {}", schedules.size(), farmerCropVarietyId);

        List<ScheduleDayDTO> allScheduleDays = new ArrayList<>();
        for (FarmerCropVarietySchedule schedule : schedules) {
            List<FarmerScheduleDay> sortedDays = schedule.getScheduleDays() == null ? List.of() : schedule.getScheduleDays().stream()
                    .sorted(Comparator.comparing(FarmerScheduleDay::getDayNumber))
                    .collect(Collectors.toList());

            if (sortedDays.isEmpty()) {
                continue;
            }

            long batchMasterStartDay = resolveLastSentMasterDay(schedule) - sortedDays.size() + 1;
            for (int index = 0; index < sortedDays.size(); index++) {
                allScheduleDays.add(convertFarmerDayToScheduleDay(sortedDays.get(index), batchMasterStartDay + index));
            }
        }
        logger.info("Merged all schedule days into a single flat array. Total days: {}", allScheduleDays.size());

        String mobile = farmer.getUser() != null ? farmer.getUser().getMobile() : null;
        String cropName = farmerCropVariety.getCropVariety() != null && farmerCropVariety.getCropVariety().getCrop() != null
                ? farmerCropVariety.getCropVariety().getCrop().getName() : null;
        String cropVarietyName = farmerCropVariety.getCropVariety() != null
                ? farmerCropVariety.getCropVariety().getName() : null;
        logger.info("Preparing FarmerScheduleResponse for Farmer ID: {}, FarmerCropVariety ID: {}, Crop Name: {}, Crop Variety Name: {}",
                farmer.getId(), farmerCropVarietyId, cropName, cropVarietyName);

        logger.info("Returning FarmerScheduleResponse for Farmer ID: {}, FarmerCropVariety ID: {}, Total Days: {}",
                farmer.getId(), farmerCropVarietyId, allScheduleDays.size());
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
                .scheduleDays(allScheduleDays)
                .build();
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

    private ScheduleDayDTO convertMasterDayToScheduleDay(MasterScheduleDay day, Long farmerDayNumber) {
        logger.info("Converting MasterScheduleDay with ID: {} to ScheduleDayDTO", day.getId());

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
        logger.info("Converted {} MasterScheduleTask entities to ScheduleTaskDTOs for MasterScheduleDay ID: {}", taskDTOs.size(), day.getId());

        return ScheduleDayDTO.builder()
                .dayNumber(farmerDayNumber)
                .masterDayNumber(day.getDayNumber())
                .dayTitle(day.getTitle())
                .dayDescription(day.getDescription())
                .tasks(taskDTOs)
                .build();
    }

    private FarmerScheduleDay convertRequestToFarmerDay(SendScheduleDayRequest request, FarmerCropVarietySchedule schedule,
                                                        UserProfile farmer, FarmerCropVariety farmerCropVariety) {
        logger.info("Converting SendScheduleDayRequest for day number: {} to FarmerScheduleDay entity", request.getDayNumber());
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
        logger.info("Converted {} SendScheduleTaskRequest to FarmerScheduleTask entities for day number: {}", tasks.size(), request.getDayNumber());

        return day;
    }

    private FarmerScheduleTask convertRequestToFarmerTask(SendScheduleTaskRequest request, FarmerScheduleDay day) {

        logger.info("Converting SendScheduleTaskRequest for task type: {} to FarmerScheduleTask entity", request.getTaskType());
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

    private ScheduleDayDTO convertFarmerDayToScheduleDay(FarmerScheduleDay day, Long masterDayNumber) {
        logger.info("Converting FarmerScheduleDay with ID: {} to ScheduleDayDTO", day.getId());

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

        logger.info("Converted {} FarmerScheduleTask entities to ScheduleTaskDTOs for FarmerScheduleDay ID: {}", tasks.size(), day.getId());
        return ScheduleDayDTO.builder()
                .dayNumber(day.getDayNumber())
                .masterDayNumber(masterDayNumber)
                .dayTitle(day.getTitle())
                .dayDescription(day.getDescription())
                .tasks(tasks)
                .build();
    }

    private Optional<FarmerCropVarietySchedule> getLatestSchedule(Long farmerId, Long farmerCropVarietyId) {
        return farmerCropVarietyScheduleRepository.findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(farmerId, farmerCropVarietyId);
    }

    private long getTotalGapDays(Long farmerId, Long farmerCropVarietyId) {
        return Optional.ofNullable(farmerScheduleGapRepository.getTotalGapDays(farmerId, farmerCropVarietyId)).orElse(0L);
    }

    private long resolveLastSentMasterDay(FarmerCropVarietySchedule schedule) {
        return schedule.getLastSentMasterDay() != null ? schedule.getLastSentMasterDay() : schedule.getLastSentDay();
    }

    private void validateSendScheduleRequest(SendScheduleRequest request, List<SendScheduleDayRequest> sortedDays, long expectedStartDay) {
        if (request.getNumberOfDays() != null && request.getNumberOfDays() != sortedDays.size()) {
            logger.warn("numberOfDays ({}) does not match scheduleDays size ({}) for Farmer ID: {}, FarmerCropVariety ID: {}. Continuing with actual scheduleDays size for master-day progression.",
                    request.getNumberOfDays(), sortedDays.size(), request.getFarmerId(), request.getFarmerCropVarietyId());
        }

//        long expectedDayNumber = expectedStartDay;
//        for (SendScheduleDayRequest dayRequest : sortedDays) {
//            logger.info("Day number = {} and expectedDayNumber = {}",dayRequest.getDayNumber(), expectedDayNumber);
//            if (!Objects.equals(dayRequest.getDayNumber(), expectedDayNumber)) {
//                throw new VPException("Schedule days must be contiguous and start from farmer day " + expectedStartDay, 400);
//            }
//            expectedDayNumber++;
//        }
    }
}
