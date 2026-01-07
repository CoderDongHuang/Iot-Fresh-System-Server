package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

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

    // 3. 温度历史数据接口 - 按前端要求格式化返回
    @GetMapping("/temperature/{vid}")
    public ApiResponse<Map<String, Object>> getTemperatureHistory(@PathVariable String vid,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                                  @RequestParam(required = false) String timeRange) {
        // 这里可以实现专门的温度历史数据获取
        // 为简化，我们复用通用历史数据方法
        ApiResponse<List<DeviceDataDto>> response = dataService.getDeviceHistoryData(vid, startTime, endTime);
        
        if (response.isSuccess()) {
            // 构建前端期望的格式
            Map<String, Object> result = Map.of("list", response.getData());
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(response.getMsg());
        }
    }

    // 4. 光照历史数据接口 - 按前端要求格式化返回
    @GetMapping("/light/{vid}")
    public ApiResponse<List<Map<String, Object>>> getLightHistory(@PathVariable String vid,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                                 @RequestParam(required = false) String timeRange) {
        // 使用专门的光照数据服务
        return dataService.getLightDataByVid(vid, startTime, endTime);
    }

    @GetMapping("/export")
    public ApiResponse<String> exportData() {
        return ApiResponse.success("数据导出功能");
    }
}