package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.AddScheduleGapRequest;
import com.example.vp.consultancy.dto.AddScheduleGapResponse;
import com.example.vp.consultancy.dto.AddScheduleTaskRequest;
import com.example.vp.consultancy.dto.EditScheduleDayResponse;
import com.example.vp.consultancy.dto.EditScheduleTaskResponse;
import com.example.vp.consultancy.dto.FarmerScheduleResponse;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewRequest;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewResponse;
import com.example.vp.consultancy.dto.SendScheduleRequest;
import com.example.vp.consultancy.dto.SendScheduleResponse;
import com.example.vp.consultancy.dto.UpdateScheduleDayRequest;
import com.example.vp.consultancy.dto.UpdateScheduleTaskRequest;
import com.example.vp.consultancy.entity.UserProfile;

public interface SendScheduleService {
    GetNextSchedulePreviewResponse getNextSchedulePreview(GetNextSchedulePreviewRequest request);
    SendScheduleResponse sendSchedule(SendScheduleRequest request);
    AddScheduleGapResponse addScheduleGap(AddScheduleGapRequest request);
    FarmerScheduleResponse getFarmerSchedule(UserProfile farmerProfile, Long farmerCropVarietyId);
    
    EditScheduleDayResponse updateScheduleDay(Long farmerId, Long farmerCropVarietyId, Long dayNumber, UpdateScheduleDayRequest request);
    EditScheduleTaskResponse addTaskToScheduleDay(Long farmerId, Long farmerCropVarietyId, Long dayNumber, AddScheduleTaskRequest request);
    EditScheduleTaskResponse updateScheduleTask(Long farmerId, Long farmerCropVarietyId, Long dayNumber, Long taskId, UpdateScheduleTaskRequest request);
    EditScheduleTaskResponse deleteScheduleTask(Long farmerId, Long farmerCropVarietyId, Long dayNumber, Long taskId);
}
