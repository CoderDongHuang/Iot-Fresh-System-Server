package com.iot.fresh.service;

import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.dto.ApiResponse;

import java.util.List;

public interface AlarmService {
    ApiResponse<List<AlarmDto>> getAllAlarms();
    ApiResponse<List<AlarmDto>> getAlarmsByDeviceId(Long deviceId);
    ApiResponse<List<AlarmDto>> getAlarmsByStatus(String status);
    ApiResponse<AlarmDto> createAlarm(AlarmDto alarmDto);
    ApiResponse<AlarmDto> resolveAlarm(Long id);
}