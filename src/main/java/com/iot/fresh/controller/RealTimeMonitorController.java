package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.service.DeviceService;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实时监控控制器
 * 提供实时数据监控相关API接口
 * 
 * @author donghuang
 * @since 2026
 */
@RestController
@RequestMapping("/api/device/monitor")
public class RealTimeMonitorController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DataService dataService;

    /**
     * 获取所有设备的实时数据
     * 
     * 路径: GET /api/device/all-real-time-data
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": [
     *     {
     *       "id": 1,
     *       "vid": "DEV001",
     *       "deviceName": "冷藏箱001",
     *       "deviceType": "冷藏箱",
     *       "location": "仓库A区",
     *       "tin": 25.5,
     *       "tout": 23.0,
     *       "lxin": 300,
     *       "brightness": 80,
     *       "speedM1": 1200,
     *       "speedM2": 1000,
     *       "vStatus": 0,
     *       "timestamp": "2024-12-19T10:30:00Z"
     *     },
     *     ...
     *   ]
     * }
     * 
     * @return ApiResponse<List<Map<String, Object>>> 包含所有设备实时数据的响应对象
     * @author System
     * @since 2024
     */
    @GetMapping("/all-real-time-data")
    public ApiResponse<List<Map<String, Object>>> getAllRealTimeData() {
        try {
            // 获取所有设备信息
            ApiResponse<List<DeviceDto>> deviceResponse = deviceService.getAllDevices();
            
            if (!deviceResponse.isSuccess()) {
                return ApiResponse.error("获取设备列表失败: " + deviceResponse.getMsg());
            }
            
            List<DeviceDto> devices = deviceResponse.getData();
            
            // 创建结果列表
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 为每个设备获取最新的数据
            for (DeviceDto device : devices) {
                Map<String, Object> deviceDataMap = new HashMap<>();
                
                // 基本设备信息
                deviceDataMap.put("id", device.getId());
                deviceDataMap.put("vid", device.getVid());
                deviceDataMap.put("deviceName", device.getDeviceName());
                deviceDataMap.put("deviceType", device.getDeviceType());
                deviceDataMap.put("location", device.getLocation());
                
                // 获取设备最新数据
                DeviceData latestData = dataService.getLatestDeviceData(device.getVid());
                
                if (latestData != null) {
                    deviceDataMap.put("tin", latestData.getTin());
                    deviceDataMap.put("tout", latestData.getTout());
                    deviceDataMap.put("hin", latestData.getHin());
                    deviceDataMap.put("hout", latestData.getHout());
                    deviceDataMap.put("lxin", latestData.getLxin());
                    deviceDataMap.put("lxout", latestData.getLxout());
                    deviceDataMap.put("brightness", latestData.getBrightness());
                    deviceDataMap.put("vStatus", latestData.getVstatus());
                    
                    // 格式化时间戳为ISO 8601字符串格式
                    if (latestData.getTimestamp() != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                        deviceDataMap.put("timestamp", latestData.getTimestamp().format(formatter) + "Z");
                    } else if (latestData.getCreatedAt() != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                        deviceDataMap.put("timestamp", latestData.getCreatedAt().format(formatter) + "Z");
                    }
                } else {
                    // 如果没有实时数据，设置默认值
                    deviceDataMap.put("tin", null);
                    deviceDataMap.put("tout", null);
                    deviceDataMap.put("lxin", null);
                    deviceDataMap.put("brightness", null);
                    deviceDataMap.put("speedM1", null);
                    deviceDataMap.put("speedM2", null);
                    deviceDataMap.put("vStatus", 1); // 离线状态
                    deviceDataMap.put("timestamp", null);
                }
                
                result.add(deviceDataMap);
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取实时数据失败: " + e.getMessage());
        }
    }
}