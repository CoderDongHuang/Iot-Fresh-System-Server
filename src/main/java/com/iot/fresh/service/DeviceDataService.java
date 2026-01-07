package com.iot.fresh.service;

import com.iot.fresh.dto.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DeviceDataService {
    ApiResponse<List<Map<String, Object>>> getLightDataByVid(String vid, LocalDateTime startTime, LocalDateTime endTime);
}