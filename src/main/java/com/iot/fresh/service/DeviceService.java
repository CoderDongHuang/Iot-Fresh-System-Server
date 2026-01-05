package com.iot.fresh.service;

import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;

import java.util.List;

public interface DeviceService {
    ApiResponse<List<DeviceDto>> getAllDevices();
    ApiResponse<PaginatedResponse<DeviceDto>> getDevicesWithPagination(Integer pageNum, Integer pageSize);
    ApiResponse<DeviceDto> getDeviceByVid(String vid);
    ApiResponse<DeviceDto> updateDevice(String vid, DeviceDto deviceDto);
    ApiResponse<DeviceDto> registerDevice(DeviceDto deviceDto);
    void updateDeviceHeartbeat(String vid);
}