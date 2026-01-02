package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private DataService dataService;

    @GetMapping("/history")
    public ApiResponse<List<DeviceDataDto>> getHistoryData(@RequestParam String vid,
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

    @GetMapping("/temperature/{vid}")
    public ApiResponse<List<DeviceDataDto>> getTemperatureHistory(@PathVariable String vid,
                                                                  @RequestParam(required = false) Long startTime,
                                                                  @RequestParam(required = false) Long endTime) {
        // 这里可以实现专门的温度历史数据获取
        // 为简化，我们复用通用历史数据方法
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

    @GetMapping("/export")
    public ApiResponse<String> exportData() {
        return ApiResponse.success("数据导出功能");
    }
}