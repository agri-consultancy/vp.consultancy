package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.FarmerScheduleRequest;
import com.example.vp.consultancy.dto.FarmerScheduleResponse;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewRequest;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewResponse;
import com.example.vp.consultancy.dto.SendScheduleRequest;
import com.example.vp.consultancy.dto.SendScheduleResponse;
import com.example.vp.consultancy.entity.UserProfile;

public interface SendScheduleService {
    GetNextSchedulePreviewResponse getNextSchedulePreview(GetNextSchedulePreviewRequest request);
    SendScheduleResponse sendSchedule(SendScheduleRequest request);
    FarmerScheduleResponse getFarmerSchedule(UserProfile farmerProfile, Long farmerCropVarietyId);
}
