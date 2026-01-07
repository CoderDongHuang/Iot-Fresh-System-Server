package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
public class DeviceDataController {

    @Autowired
    private DataService dataService;

    // 8. 设备历史数据接口
    @GetMapping("/history-data/{vid}")
    public ApiResponse<List<Map<String, Object>>> getHistoryData(@PathVariable String vid,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                                 @RequestParam(required = false) String timeRange) {
        // 获取设备历史数据
        ApiResponse<List<DeviceDataDto>> response = dataService.getDeviceHistoryData(vid, startTime, endTime);
        
        if (response.isSuccess()) {
            // 将DeviceDataDto列表转换为前端期望的格式
            List<Map<String, Object>> result = response.getData().stream().map(data -> {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("tin", data.getTin());
                item.put("tout", data.getTout());
                item.put("lxin", data.getLxin());
                item.put("vStatus", data.getVstatus());
                item.put("timestamp", data.getTimestamp() != null ? data.getTimestamp().toString() : data.getCreatedAt().toString());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(response.getMsg());
        }
    }
}