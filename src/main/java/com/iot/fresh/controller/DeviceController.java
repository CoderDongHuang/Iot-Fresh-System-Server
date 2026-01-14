package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.dto.PaginatedResponse;
import com.iot.fresh.service.DeviceService;
import com.iot.fresh.service.DataService;
import com.iot.fresh.service.DeviceDataProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private DataService dataService;
    
    @Autowired
    private DeviceDataProcessor deviceDataProcessor;

    // 此端点与DeviceManagementController中的/api/device/list有相似功能，为避免冲突暂时注释
    /*@GetMapping
    public ApiResponse<PaginatedResponse<DeviceDto>> getDevices(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("Debug - DeviceController.getDevices called with pageNum: " + pageNum + ", pageSize: " + pageSize);
        return deviceService.getDevicesWithPagination(pageNum, pageSize);
    }*/
    
    // 为向前兼容添加额外的端点（注意：此端点与DeviceManagementController中的/api/device/list可能冲突，仅保留用于向后兼容）
    @GetMapping("/list-all")
    public ApiResponse<PaginatedResponse<DeviceDto>> getDevicesList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("Debug - DeviceController.getDevicesList called with pageNum: " + pageNum + ", pageSize: " + pageSize);
        return deviceService.getDevicesWithPagination(pageNum, pageSize);
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
    public ApiResponse<List<Map<String, Object>>> getDeviceHistoryData(@PathVariable String vid,
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
        
        // 获取设备历史数据
        var response = dataService.getDeviceHistoryData(vid, startDateTime, endDateTime);
        
        if (response.isSuccess()) {
            // 将DeviceDataDto列表转换为前端期望的格式
            List<Map<String, Object>> result = response.getData().stream()
                .filter(data -> data.getTimestamp() != null || data.getCreatedAt() != null) // 过滤掉没有时间戳的数据
                .map(data -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    
                    // 使用ISO 8601格式的时间字符串
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    item.put("timestamp", data.getTimestamp() != null ? 
                        data.getTimestamp().format(formatter) : 
                        data.getCreatedAt().format(formatter));
                    
                    // 包含各种传感器数据
                    if (data.getTin() != null) item.put("tin", data.getTin());
                    if (data.getTout() != null) item.put("tout", data.getTout());
                    if (data.getHin() != null) item.put("hin", data.getHin());
                    if (data.getHout() != null) item.put("hout", data.getHout());
                    if (data.getLxin() != null) item.put("lxin", data.getLxin());
                    if (data.getBrightness() != null) item.put("brightness", data.getBrightness());
                    
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
            
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(response.getMsg());
        }
    }

    @PostMapping("/{vid}/control")
    public ApiResponse<String> controlDevice(@PathVariable String vid, @RequestBody Object controlData) {
        // 将控制命令发送到设备
        deviceDataProcessor.sendControlCommand(vid, controlData.toString());
        return ApiResponse.success("设备控制命令发送成功", "OK");
    }
}