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
@RequestMapping("/api/device/management")
public class DeviceManagementController {

    @Autowired
    private DeviceManagementService deviceManagementService;

    // 1. 设备列表接口 - 返回前端期望的标准格式
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getDeviceList(
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
                case "error":
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
        
        // 调用服务层获取数据
        ApiResponse<PaginatedResponse<DeviceDto>> response = deviceManagementService.getDeviceList(pageNum, pageSize, keyword, statusInt);
        
        // 转换为前端期望的格式
        if (response.isSuccess() && response.getData() != null) {
            PaginatedResponse<DeviceDto> paginatedData = response.getData();
            
            // 转换为前端期望的格式
            List<Map<String, Object>> formattedList = new java.util.ArrayList<>();
            for (DeviceDto device : paginatedData.getList()) {
                Map<String, Object> deviceMap = new java.util.HashMap<>();
                deviceMap.put("vid", device.getVid());
                deviceMap.put("deviceName", device.getDeviceName());
                deviceMap.put("deviceType", device.getDeviceType());
                deviceMap.put("status", device.getStatus());
                deviceMap.put("location", device.getLocation());
                deviceMap.put("manufacturer", device.getManufacturer());
                deviceMap.put("model", device.getModel());
                deviceMap.put("firmwareVersion", device.getFirmwareVersion());
                if (device.getLastHeartbeat() != null) {
                    // 格式化时间为 "yyyy-MM-dd HH:mm:ss" 格式
                    deviceMap.put("lastOnlineTime", device.getLastHeartbeat().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    deviceMap.put("lastOnlineTime", null);
                }
                formattedList.add(deviceMap);
            }
            
            Map<String, Object> frontendData = new java.util.HashMap<>();
            frontendData.put("list", formattedList);
            frontendData.put("total", paginatedData.getTotal());
            frontendData.put("pageNum", paginatedData.getPageNum());
            frontendData.put("pageSize", paginatedData.getPageSize());
            
            return ApiResponse.success("操作成功", frontendData);
        } else {
            return ApiResponse.error(response.getCode(), response.getMessage());
        }
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