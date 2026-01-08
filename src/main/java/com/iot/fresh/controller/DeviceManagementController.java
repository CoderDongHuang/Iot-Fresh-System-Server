package com.iot.fresh.controller;

import com.iot.fresh.dto.*;
import com.iot.fresh.service.DeviceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
public class DeviceManagementController {

    @Autowired
    private DeviceManagementService deviceManagementService;

    // 1. 设备列表接口
    @GetMapping("/list")
    public ApiResponse<PaginatedResponse<DeviceDto>> getDeviceList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        // 将字符串状态转换为整数状态
        Integer statusInt = null;
        if (status != null) {
            switch (status.toLowerCase()) {
                case "online":
                    statusInt = 1;
                    break;
                case "offline":
                    statusInt = 0;
                    break;
                case "fault":
                    statusInt = 2;
                    break;
                case "maintenance":
                    statusInt = 3;
                    break;
                default:
                    try {
                        statusInt = Integer.parseInt(status);
                    } catch (NumberFormatException e) {
                        // 如果无法解析为整数，则忽略状态过滤
                        statusInt = null;
                    }
                    break;
            }
        }
        
        return deviceManagementService.getDeviceList(pageNum, pageSize, keyword, statusInt);
    }

    // 2. 设备详情接口
    @GetMapping("/detail/{vid}")
    public ApiResponse<DeviceDetailDto> getDeviceDetail(@PathVariable String vid) {
        return deviceManagementService.getDeviceDetail(vid);
    }

    // 5. 设备实时数据接口
    @GetMapping("/real-time-data/{vid}")
    public ApiResponse<DeviceCurrentDataDto> getRealTimeData(@PathVariable String vid) {
        return deviceManagementService.getRealTimeData(vid);
    }

    // 6. 设备状态统计接口
    @GetMapping("/status-stats")
    public ApiResponse<DeviceStatusStatsDto> getStatusStats() {
        return deviceManagementService.getStatusStats();
    }

    // 7. 设备控制接口
    @PostMapping("/control/{vid}")
    public ApiResponse<String> controlDevice(@PathVariable String vid, @RequestBody Map<String, Object> controlCommand) {
        return deviceManagementService.controlDevice(vid, controlCommand);
    }
}