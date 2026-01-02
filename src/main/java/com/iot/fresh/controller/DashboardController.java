package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        return dashboardService.getStatistics();
    }

    @GetMapping("/devices/status")
    public ApiResponse<Map<String, Object>> getDeviceStatusDistribution() {
        return dashboardService.getDeviceStatusDistribution();
    }
}