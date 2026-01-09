package com.iot.fresh.service;

import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;
import java.util.Map;

public interface AlarmService {
    ApiResponse<PaginatedResponse<Map<String, Object>>> getAlarmList(Integer pageNum, Integer pageSize, String level, String status, String keyword);
    ApiResponse<String> resolveAlarm(Long alarmId);
    ApiResponse<String> ignoreAlarm(Long alarmId);
    ApiResponse<String> clearAllAlarms();
    ApiResponse<Map<String, Object>> getAlarmDetail(Long alarmId);
    ApiResponse<Map<String, Object>> getAlarmStatistics();
    
    // 新增方法
    void processAlarm(AlarmDataDto alarmData);
    void createAlarm(AlarmDto alarmDto);
}