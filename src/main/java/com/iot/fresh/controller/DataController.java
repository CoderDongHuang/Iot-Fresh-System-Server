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

    // 注释掉温度历史数据接口 - 不再需要图表功能
    /*
    @GetMapping("/temperature/{vid}")
    public ApiResponse<Map<String, Object>> getTemperatureHistory(@PathVariable String vid,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                                  @RequestParam(required = false) String timeRange) {
        // 如果指定了timeRange，根据timeRange计算开始和结束时间
        if (timeRange != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (timeRange.toLowerCase()) {
                case "1h":
                    startTime = now.minusHours(1);
                    break;
                case "6h":
                    startTime = now.minusHours(6);
                    break;
                case "24h":
                    startTime = now.minusHours(24);
                    break;
                case "7d":
                    startTime = now.minusDays(7);
                    break;
                case "30d":
                    startTime = now.minusDays(30);
                    break;
                default:
                    // 如果timeRange无效，使用默认的24小时
                    startTime = now.minusHours(24);
                    break;
            }
            if (endTime == null) {
                endTime = now;
            }
        } else {
            // 如果没有指定timeRange，使用默认的24小时
            if (startTime == null) {
                startTime = LocalDateTime.now().minusHours(24);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
        }

        // 获取设备历史数据
        ApiResponse<List<DeviceDataDto>> response = dataService.getDeviceHistoryData(vid, startTime, endTime);
        
        if (response.isSuccess()) {
            // 将DeviceDataDto列表转换为前端TemperatureChart期望的格式
            List<Map<String, Object>> temperatureDataList = response.getData().stream()
                .filter(data -> data.getTimestamp() != null || data.getCreatedAt() != null) // 过滤掉没有时间戳的数据
                .map(data -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    
                    // 使用ISO 8601格式的时间字符串
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    item.put("timestamp", data.getTimestamp() != null ? 
                        data.getTimestamp().format(formatter) : 
                        data.getCreatedAt().format(formatter));
                    
                    // 温度数据 - 确保提供所有必需字段
                    item.put("tin", data.getTin() != null ? data.getTin() : 0.0);  // 如果为空则设为0.0
                    item.put("tout", data.getTout() != null ? data.getTout() : 0.0); // 如果为空则设为0.0
                    
                    // 添加温度上下限 - 从设备配置获取或使用默认值
                    // 实际应用中可以从设备配置中获取特定设备的温控范围
                    item.put("tinDH", 30.0); // 温度上限
                    item.put("tinDL", 10.0); // 温度下限
                    
                    return item;
                })
                .sorted((a, b) -> {
                    String timeA = (String) a.get("timestamp");
                    String timeB = (String) b.get("timestamp");
                    if (timeA == null || timeB == null) {
                        return 0; // 如果任一时间戳为null，视为相等
                    }
                    return timeA.compareTo(timeB);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 构建前端期望的格式
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("list", temperatureDataList);
            
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(response.getMsg());
        }
    }
    */

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