package com.example.vp.consultancy.controller;

import com.example.vp.consultancy.dto.ApiResponse;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewRequest;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewResponse;
import com.example.vp.consultancy.dto.SendScheduleRequest;
import com.example.vp.consultancy.dto.SendScheduleResponse;
import com.example.vp.consultancy.service.SendScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final SendScheduleService sendScheduleService;

    @GetMapping("/schedules/preview")
    public ResponseEntity<ApiResponse<GetNextSchedulePreviewResponse>> getNextSchedulePreview(
            @Valid @ModelAttribute GetNextSchedulePreviewRequest request) {
        GetNextSchedulePreviewResponse response = sendScheduleService.getNextSchedulePreview(request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Next schedule preview retrieved successfully",
                response,
                HttpStatus.OK.value()
        ));
    }

    @PostMapping("/schedules/send")
    public ResponseEntity<ApiResponse<SendScheduleResponse>> sendSchedule(@Valid @RequestBody SendScheduleRequest request) {
        SendScheduleResponse response = sendScheduleService.sendSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Schedule sent successfully",
                response,
                HttpStatus.CREATED.value()
        ));
    }
}
