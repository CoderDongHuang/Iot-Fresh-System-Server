package com.iot.fresh.service;

import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.dto.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DataService {
    ApiResponse<DeviceDataDto> saveDeviceData(DeviceDataDto deviceDataDto);
    ApiResponse<List<DeviceDataDto>> getDeviceRealTimeData(String vid);
    ApiResponse<List<DeviceDataDto>> getDeviceHistoryData(String vid, LocalDateTime startTime, LocalDateTime endTime);
    ApiResponse<List<Map<String, Object>>> getLightDataByVid(String vid, LocalDateTime startTime, LocalDateTime endTime);
    void processDeviceDataFromMqtt(String vid, String payload);
    void updateDeviceStatus(String vid, Integer status);
}