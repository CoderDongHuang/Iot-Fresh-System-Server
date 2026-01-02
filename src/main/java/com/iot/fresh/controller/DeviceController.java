package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.service.DeviceService;
import com.iot.fresh.service.DataService;
import com.iot.fresh.service.DeviceDataProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private DataService dataService;
    
    @Autowired
    private DeviceDataProcessor deviceDataProcessor;

    @GetMapping
    public ApiResponse<List<DeviceDto>> getDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{vid}")
    public ApiResponse<DeviceDto> getDevice(@PathVariable String vid) {
        return deviceService.getDeviceByVid(vid);
    }

    @GetMapping("/{vid}/real-time")
    public ApiResponse<?> getDeviceRealTimeData(@PathVariable String vid) {
        return dataService.getDeviceRealTimeData(vid);
    }

    @GetMapping("/{vid}/history")
    public ApiResponse<?> getDeviceHistoryData(@PathVariable String vid,
                                               @RequestParam(required = false) Long startTime,
                                               @RequestParam(required = false) Long endTime) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startTime != null) {
            startDateTime = LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC);
        }
        if (endTime != null) {
            endDateTime = LocalDateTime.ofEpochSecond(endTime, 0, ZoneOffset.UTC);
        }
        
        return dataService.getDeviceHistoryData(vid, startDateTime, endDateTime);
    }

    @PostMapping("/{vid}/control")
    public ApiResponse<String> controlDevice(@PathVariable String vid, @RequestBody Object controlData) {
        // 将控制命令发送到设备
        deviceDataProcessor.sendControlCommand(vid, controlData.toString());
        return ApiResponse.success("设备控制命令发送成功", "OK");
    }
}