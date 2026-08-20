package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.dto.AddScheduleGapRequest;
import com.example.vp.consultancy.dto.AddScheduleGapResponse;
import com.example.vp.consultancy.dto.ApiResponse;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewRequest;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewResponse;
import com.example.vp.consultancy.dto.SendScheduleRequest;
import com.example.vp.consultancy.dto.SendScheduleResponse;
import com.example.vp.consultancy.service.SendScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultant")
@PreAuthorize("hasRole('CONSULTANT')")
@Validated
@RequiredArgsConstructor
public class SendScheduleController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SendScheduleController.class);
    private final SendScheduleService sendScheduleService;

    @GetMapping("/schedules/preview")
    public ResponseEntity<ApiResponse<GetNextSchedulePreviewResponse>> getNextSchedulePreview(
            @Valid @ModelAttribute GetNextSchedulePreviewRequest request) {
        logger.info("Received request to get next schedule preview: {}", request);
        GetNextSchedulePreviewResponse response = sendScheduleService.getNextSchedulePreview(request);
        logger.info("Next schedule preview retrieved successfully: {}", response);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Next schedule preview retrieved successfully",
                response,
                HttpStatus.OK.value()
        ));
    }

    @PostMapping("/schedules/send")
    public ResponseEntity<ApiResponse<SendScheduleResponse>> sendSchedule(@Valid @RequestBody SendScheduleRequest request) {
        logger.info("Received request to send schedule: {}", request);
        SendScheduleResponse response = sendScheduleService.sendSchedule(request);
        logger.info("Schedule sent successfully: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Schedule sent successfully",
                response,
                HttpStatus.CREATED.value()
        ));
    }

    @PostMapping("/schedules/gap")
    public ResponseEntity<ApiResponse<AddScheduleGapResponse>> addScheduleGap(@Valid @RequestBody AddScheduleGapRequest request) {
        logger.info("Received request to add schedule gap: {}", request);
        AddScheduleGapResponse response = sendScheduleService.addScheduleGap(request);
        logger.info("Schedule gap added successfully: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Schedule gap added successfully",
                response,
                HttpStatus.CREATED.value()
        ));
    }
}
