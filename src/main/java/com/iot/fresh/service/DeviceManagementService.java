package com.iot.fresh.service;

import com.iot.fresh.dto.*;
import java.util.Map;

public interface DeviceManagementService {
    ApiResponse<PaginatedResponse<DeviceDto>> getDeviceList(Integer pageNum, Integer pageSize, String keyword, Integer status);
    ApiResponse<DeviceDetailDto> getDeviceDetail(String vid);
    ApiResponse<DeviceCurrentDataDto> getRealTimeData(String vid);
    ApiResponse<DeviceStatusStatsDto> getStatusStats();
    ApiResponse<String> controlDevice(String vid, Map<String, Object> controlCommand);
}