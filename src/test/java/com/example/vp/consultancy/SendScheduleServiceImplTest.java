package com.example.vp.consultancy;

import com.example.vp.consultancy.dto.AddScheduleGapRequest;
import com.example.vp.consultancy.dto.AddScheduleGapResponse;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewRequest;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewResponse;
import com.example.vp.consultancy.dto.SendScheduleDayRequest;
import com.example.vp.consultancy.dto.SendScheduleRequest;
import com.example.vp.consultancy.dto.SendScheduleResponse;
import com.example.vp.consultancy.dto.SendScheduleTaskRequest;
import com.example.vp.consultancy.entity.Crop;
import com.example.vp.consultancy.entity.CropVariety;
import com.example.vp.consultancy.entity.FarmerCropVariety;
import com.example.vp.consultancy.entity.FarmerCropVarietySchedule;
import com.example.vp.consultancy.entity.FarmerScheduleDay;
import com.example.vp.consultancy.entity.FarmerScheduleGap;
import com.example.vp.consultancy.entity.MasterScheduleDay;
import com.example.vp.consultancy.entity.MasterScheduleTemplate;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.VPException;
import com.example.vp.consultancy.repository.FarmerCropVarietyRepository;
import com.example.vp.consultancy.repository.FarmerCropVarietyScheduleRepository;
import com.example.vp.consultancy.repository.FarmerScheduleDayRepository;
import com.example.vp.consultancy.repository.FarmerScheduleGapRepository;
import com.example.vp.consultancy.repository.FarmerScheduleTaskRepository;
import com.example.vp.consultancy.repository.MasterScheduleDayRepository;
import com.example.vp.consultancy.repository.MasterScheduleTemplateRepository;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.service.impl.SendScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendScheduleServiceImplTest {

    private UserProfileRepository userProfileRepository;
    private FarmerCropVarietyRepository farmerCropVarietyRepository;
    private MasterScheduleTemplateRepository masterScheduleTemplateRepository;
    private MasterScheduleDayRepository masterScheduleDayRepository;
    private FarmerCropVarietyScheduleRepository farmerCropVarietyScheduleRepository;
    private FarmerScheduleGapRepository farmerScheduleGapRepository;
    private FarmerScheduleDayRepository farmerScheduleDayRepository;
    private FarmerScheduleTaskRepository farmerScheduleTaskRepository;
    private SendScheduleServiceImpl service;

    @BeforeEach
    void setUp() {
        userProfileRepository = mock(UserProfileRepository.class);
        farmerCropVarietyRepository = mock(FarmerCropVarietyRepository.class);
        masterScheduleTemplateRepository = mock(MasterScheduleTemplateRepository.class);
        masterScheduleDayRepository = mock(MasterScheduleDayRepository.class);
        farmerCropVarietyScheduleRepository = mock(FarmerCropVarietyScheduleRepository.class);
        farmerScheduleGapRepository = mock(FarmerScheduleGapRepository.class);
        farmerScheduleDayRepository = mock(FarmerScheduleDayRepository.class);
        farmerScheduleTaskRepository = mock(FarmerScheduleTaskRepository.class);

        service = new SendScheduleServiceImpl(
                userProfileRepository,
                farmerCropVarietyRepository,
                masterScheduleTemplateRepository,
                masterScheduleDayRepository,
                farmerCropVarietyScheduleRepository,
                farmerScheduleGapRepository,
                farmerScheduleDayRepository,
                farmerScheduleTaskRepository
        );
    }

    @Test
    void getNextSchedulePreviewWithoutGapKeepsExistingBehavior() {
        UserProfile farmer = createFarmer();
        FarmerCropVariety farmerCropVariety = createFarmerCropVariety(farmer);
        MasterScheduleTemplate template = createTemplate();

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(farmer));
        when(farmerCropVarietyRepository.findById(100L)).thenReturn(Optional.of(farmerCropVariety));
        when(masterScheduleTemplateRepository.findById(200L)).thenReturn(Optional.of(template));
        when(farmerCropVarietyScheduleRepository.findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(createSchedule(3L, 3L)));
        when(farmerScheduleGapRepository.getTotalGapDays(1L, 100L)).thenReturn(0L);
        when(masterScheduleDayRepository.findByTemplateIdAndDayNumberBetweenOrderByDayNumberAsc(200L, 4L, 5L))
                .thenReturn(List.of(createMasterDay(4L, "Day 4"), createMasterDay(5L, "Day 5")));

        GetNextSchedulePreviewResponse response = service.getNextSchedulePreview(GetNextSchedulePreviewRequest.builder()
                .farmerId(1L)
                .farmerCropVarietyId(100L)
                .masterScheduleTemplateId(200L)
                .numberOfDays(2L)
                .build());

        assertEquals(4L, response.getStartDay());
        assertEquals(5L, response.getEndDay());
        assertEquals(4L, response.getMasterStartDay());
        assertEquals(5L, response.getMasterEndDay());
        assertEquals(4L, response.getScheduleDays().getFirst().getDayNumber());
        assertEquals(4L, response.getScheduleDays().getFirst().getMasterDayNumber());
    }

    @Test
    void getNextSchedulePreviewAppliesAccumulatedGapToFarmerDays() {
        UserProfile farmer = createFarmer();
        FarmerCropVariety farmerCropVariety = createFarmerCropVariety(farmer);
        MasterScheduleTemplate template = createTemplate();

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(farmer));
        when(farmerCropVarietyRepository.findById(100L)).thenReturn(Optional.of(farmerCropVariety));
        when(masterScheduleTemplateRepository.findById(200L)).thenReturn(Optional.of(template));
        when(farmerCropVarietyScheduleRepository.findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(createSchedule(3L, 3L)));
        when(farmerScheduleGapRepository.getTotalGapDays(1L, 100L)).thenReturn(5L);
        when(masterScheduleDayRepository.findByTemplateIdAndDayNumberBetweenOrderByDayNumberAsc(200L, 4L, 5L))
                .thenReturn(List.of(createMasterDay(4L, "Day 4"), createMasterDay(5L, "Day 5")));

        GetNextSchedulePreviewResponse response = service.getNextSchedulePreview(GetNextSchedulePreviewRequest.builder()
                .farmerId(1L)
                .farmerCropVarietyId(100L)
                .masterScheduleTemplateId(200L)
                .numberOfDays(2L)
                .build());

        assertEquals(9L, response.getStartDay());
        assertEquals(10L, response.getEndDay());
        assertEquals(4L, response.getMasterStartDay());
        assertEquals(5L, response.getMasterEndDay());
        assertEquals(9L, response.getScheduleDays().get(0).getDayNumber());
        assertEquals(4L, response.getScheduleDays().get(0).getMasterDayNumber());
        assertEquals(10L, response.getScheduleDays().get(1).getDayNumber());
        assertEquals(5L, response.getScheduleDays().get(1).getMasterDayNumber());
    }

    @Test
    void sendScheduleStoresFarmerAndMasterProgressSeparatelyAfterGap() {
        UserProfile farmer = createFarmer();
        FarmerCropVariety farmerCropVariety = createFarmerCropVariety(farmer);
        AtomicReference<FarmerCropVarietySchedule> savedScheduleRef = new AtomicReference<>();

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(farmer));
        when(farmerCropVarietyRepository.findById(100L)).thenReturn(Optional.of(farmerCropVariety));
        when(farmerCropVarietyScheduleRepository.findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(createSchedule(3L, 3L)));
        when(farmerScheduleGapRepository.getTotalGapDays(1L, 100L)).thenReturn(5L);
        when(farmerCropVarietyScheduleRepository.save(any(FarmerCropVarietySchedule.class))).thenAnswer(invocation -> {
            FarmerCropVarietySchedule schedule = invocation.getArgument(0);
            savedScheduleRef.set(schedule);
            schedule.setId(900L);
            return schedule;
        });

        SendScheduleRequest request = SendScheduleRequest.builder()
                .farmerId(1L)
                .farmerCropVarietyId(100L)
                .numberOfDays(2L)
                .scheduleDays(List.of(
                        createSendScheduleDay(9L, "Farmer Day 9"),
                        createSendScheduleDay(10L, "Farmer Day 10")
                ))
                .build();

        SendScheduleResponse response = service.sendSchedule(request);

        assertEquals(900L, response.getScheduleId());
        assertEquals(9L, response.getStartDay());
        assertEquals(10L, response.getEndDay());
        assertEquals(2L, response.getDaysSent());
        assertEquals(10L, savedScheduleRef.get().getLastSentDay());
        assertEquals(5L, savedScheduleRef.get().getLastSentMasterDay());
    }

    @Test
    void addScheduleGapReturnsNextFarmerAndMasterDays() {
        UserProfile farmer = createFarmer();
        FarmerCropVariety farmerCropVariety = createFarmerCropVariety(farmer);
        FarmerCropVarietySchedule latestSchedule = createSchedule(10L, 5L);

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(farmer));
        when(farmerCropVarietyRepository.findById(100L)).thenReturn(Optional.of(farmerCropVariety));
        when(farmerCropVarietyScheduleRepository.findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(latestSchedule));
        when(farmerScheduleGapRepository.getTotalGapDays(1L, 100L)).thenReturn(5L);
        when(farmerScheduleGapRepository.save(any(FarmerScheduleGap.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddScheduleGapResponse response = service.addScheduleGap(AddScheduleGapRequest.builder()
                .farmerId(1L)
                .farmerCropVarietyId(100L)
                .gapDays(2L)
                .build());

        assertEquals(2L, response.getGapDays());
        assertEquals(7L, response.getTotalGapDays());
        assertEquals(10L, response.getLastSentFarmerDay());
        assertEquals(5L, response.getLastSentMasterDay());
        assertEquals(6L, response.getNextMasterDay());
        assertEquals(13L, response.getNextFarmerDay());
    }

    @Test
    void getFarmerSchedulePreservesMasterDayMappingAcrossMultipleBatches() {
        UserProfile farmer = createFarmer();
        FarmerCropVariety farmerCropVariety = createFarmerCropVariety(farmer);

        FarmerCropVarietySchedule firstSchedule = createSchedule(3L, 3L);
        firstSchedule.setScheduleDays(List.of(
                createFarmerScheduleDay(1L, "Farmer Day 1"),
                createFarmerScheduleDay(2L, "Farmer Day 2"),
                createFarmerScheduleDay(3L, "Farmer Day 3")
        ));

        FarmerCropVarietySchedule secondSchedule = createSchedule(10L, 5L);
        secondSchedule.setScheduleDays(List.of(
                createFarmerScheduleDay(9L, "Farmer Day 9"),
                createFarmerScheduleDay(10L, "Farmer Day 10")
        ));

        when(farmerCropVarietyRepository.findById(100L)).thenReturn(Optional.of(farmerCropVariety));
        when(farmerCropVarietyScheduleRepository.findByFarmerCropVarietyIdOrderByIdAsc(100L))
                .thenReturn(List.of(firstSchedule, secondSchedule));

        var response = service.getFarmerSchedule(farmer, 100L);

        assertEquals(5, response.getScheduleDays().size());
        assertEquals(1L, response.getScheduleDays().get(0).getDayNumber());
        assertEquals(1L, response.getScheduleDays().get(0).getMasterDayNumber());
        assertEquals(3L, response.getScheduleDays().get(2).getDayNumber());
        assertEquals(3L, response.getScheduleDays().get(2).getMasterDayNumber());
        assertEquals(9L, response.getScheduleDays().get(3).getDayNumber());
        assertEquals(4L, response.getScheduleDays().get(3).getMasterDayNumber());
        assertEquals(10L, response.getScheduleDays().get(4).getDayNumber());
        assertEquals(5L, response.getScheduleDays().get(4).getMasterDayNumber());
    }

    @Test
    void addScheduleGapBeforeAnyScheduleIsRejected() {
        UserProfile farmer = createFarmer();
        FarmerCropVariety farmerCropVariety = createFarmerCropVariety(farmer);

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(farmer));
        when(farmerCropVarietyRepository.findById(100L)).thenReturn(Optional.of(farmerCropVariety));
        when(farmerCropVarietyScheduleRepository.findTopByFarmerIdAndFarmerCropVarietyIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.empty());

        VPException exception = assertThrows(VPException.class, () -> service.addScheduleGap(AddScheduleGapRequest.builder()
                .farmerId(1L)
                .farmerCropVarietyId(100L)
                .gapDays(2L)
                .build()));

        assertTrue(exception.getMessage().contains("Cannot add a schedule gap"));
    }

    private UserProfile createFarmer() {
        return UserProfile.builder()
                .id(1L)
                .firstName("Farmer")
                .lastName("One")
                .email("farmer@example.com")
                .user(User.builder().id(501L).mobile("9999999999").build())
                .build();
    }

    private FarmerCropVariety createFarmerCropVariety(UserProfile farmer) {
        Crop crop = Crop.builder().id(20L).name("Grape").build();
        CropVariety cropVariety = CropVariety.builder().id(10L).name("Thompson").crop(crop).build();
        return FarmerCropVariety.builder()
                .id(100L)
                .farmer(farmer)
                .cropVariety(cropVariety)
                .status("ACTIVE")
                .build();
    }

    private MasterScheduleTemplate createTemplate() {
        return MasterScheduleTemplate.builder()
                .id(200L)
                .cropVarietyId(10L)
                .consultantId(300L)
                .build();
    }

    private FarmerCropVarietySchedule createSchedule(Long lastSentDay, Long lastSentMasterDay) {
        return FarmerCropVarietySchedule.builder()
                .id(700L)
                .lastSentDay(lastSentDay)
                .lastSentMasterDay(lastSentMasterDay)
                .build();
    }

    private MasterScheduleDay createMasterDay(Long dayNumber, String title) {
        return MasterScheduleDay.builder()
                .id(dayNumber)
                .dayNumber(dayNumber)
                .title(title)
                .description(title + " description")
                .build();
    }

    private FarmerScheduleDay createFarmerScheduleDay(Long dayNumber, String title) {
        return FarmerScheduleDay.builder()
                .id(dayNumber)
                .dayNumber(dayNumber)
                .title(title)
                .description(title + " description")
                .tasks(List.of())
                .build();
    }

    private SendScheduleDayRequest createSendScheduleDay(Long dayNumber, String title) {
        return SendScheduleDayRequest.builder()
                .dayNumber(dayNumber)
                .dayTitle(title)
                .dayDescription(title + " description")
                .status("PENDING")
                .displayOrder(dayNumber)
                .tasks(List.of(SendScheduleTaskRequest.builder()
                        .taskType("GENERAL")
                        .taskDescription("Do work for " + title)
                        .quantity("1")
                        .proportion("1:1")
                        .priority(1L)
                        .build()))
                .build();
    }
}




