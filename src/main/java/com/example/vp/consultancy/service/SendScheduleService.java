package com.example.vp.consultancy.service;

import com.example.vp.consultancy.dto.AddScheduleGapRequest;
import com.example.vp.consultancy.dto.AddScheduleGapResponse;
import com.example.vp.consultancy.dto.FarmerScheduleResponse;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewRequest;
import com.example.vp.consultancy.dto.GetNextSchedulePreviewResponse;
import com.example.vp.consultancy.dto.SendScheduleRequest;
import com.example.vp.consultancy.dto.SendScheduleResponse;
import com.example.vp.consultancy.entity.UserProfile;

public interface SendScheduleService {
    GetNextSchedulePreviewResponse getNextSchedulePreview(GetNextSchedulePreviewRequest request);
    SendScheduleResponse sendSchedule(SendScheduleRequest request);
    AddScheduleGapResponse addScheduleGap(AddScheduleGapRequest request);
    FarmerScheduleResponse getFarmerSchedule(UserProfile farmerProfile, Long farmerCropVarietyId);
}
