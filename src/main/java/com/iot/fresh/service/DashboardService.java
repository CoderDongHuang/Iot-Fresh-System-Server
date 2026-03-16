package com.iot.fresh.service;

import com.iot.fresh.dto.ApiResponse;

import java.util.Map;

public interface DashboardService {
    ApiResponse<Map<String, Object>> getStatistics();
    ApiResponse<Map<String, Object>> getDeviceStatusDistribution();
    
    /**
     * 获取报警统计信息（用于仪表盘）
     * @return 报警统计响应对象
     */
    ApiResponse<Map<String, Object>> getAlarmStatistics();
}