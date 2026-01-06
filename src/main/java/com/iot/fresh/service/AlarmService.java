package com.iot.fresh.service;

import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.dto.AlarmStatisticsDto;
import com.iot.fresh.dto.ApiResponse;
import java.util.List;

public interface AlarmService {
    ApiResponse<String> processAlarm(AlarmDataDto alarmDataDto);
    ApiResponse<List<AlarmDto>> getAlarmsByVid(String vid);
    ApiResponse<String> handleAlarm(Long alarmId);
    
    // 添加缺失的方法
    ApiResponse<List<AlarmDto>> getAlarmsByDeviceId(Long deviceId);
    ApiResponse<List<AlarmDto>> getAlarmsByStatus(String status);
    ApiResponse<List<AlarmDto>> getAllAlarms();
    ApiResponse<AlarmDto> resolveAlarm(Long alarmId);
    
    // 添加createAlarm方法
    ApiResponse<String> createAlarm(AlarmDto alarmDto);
    
    // 添加报警统计方法
    ApiResponse<List<AlarmStatisticsDto>> getAlarmStatistics();
}