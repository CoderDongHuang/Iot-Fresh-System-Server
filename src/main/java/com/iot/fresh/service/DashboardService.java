package com.iot.fresh.service;

import com.iot.fresh.dto.ApiResponse;

import java.util.Map;

public interface DashboardService {
    ApiResponse<Map<String, Object>> getStatistics();
    ApiResponse<Map<String, Object>> getDeviceStatusDistribution();
}