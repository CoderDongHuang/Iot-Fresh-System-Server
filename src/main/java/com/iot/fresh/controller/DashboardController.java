package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.service.DataService;
import com.iot.fresh.service.DashboardService;
import com.iot.fresh.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 * 提供数据统计和分析相关API接口
 * 
 * @author donghuang
 * @since 2026
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DataService dataService;

    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private DashboardService dashboardService;

    /**
     * 数据统计接口
     * 
     * 路径: GET /api/dashboard/data-statistics
     * 
     * 请求参数:
     * - vid - 设备VID（可选）
     * - statType - 统计类型（temperature, humidity, light, comprehensive）
     * - startTime - 开始时间（可选）
     * - endTime - 结束时间（可选）
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "totalRecords": 5000,
     *     "avgTemp": 22.5,
     *     "maxTemp": 35.2,
     *     "minTemp": 5.3,
     *     "avgHumidity": 65.4,
     *     "avgLight": 200.5,
     *     "detail": [
     *       {
     *         "deviceName": "冷库A",
     *         "vid": "device001",
     *         "avgTemp": 22.5,
     *         "maxTemp": 35.2,
     *         "minTemp": 5.3,
     *         "avgHumidity": 65.4,
     *         "avgLight": 200.5,
     *         "recordCount": 1000,
     *         "timeRange": "2023-01-01 ~ 2023-12-31"
     *       }
     *     ]
     *   }
     * }
     * 
     * @param vid 设备VID（可选）
     * @param statType 统计类型（temperature, humidity, light, comprehensive）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return ApiResponse<Map<String, Object>> 包含数据统计的响应对象
     * @author donghuang
     * @since 2026
     */
    @GetMapping("/data-statistics")
    public ApiResponse<Map<String, Object>> getDataStatistics(
            @RequestParam(required = false) String vid,
            @RequestParam(defaultValue = "comprehensive") String statType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endTime) {
        
        try {
            // 设置默认时间范围
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30); // 默认过去30天
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            // 确保endTime包含当天的完整时间
            endTime = endTime.toLocalDate().atTime(23, 59, 59);

            // 根据是否有VID来决定统计范围
            if (vid != null && !vid.trim().isEmpty()) {
                // 单个设备统计
                return getSingleDeviceStatistics(vid, statType, startTime, endTime);
            } else {
                // 所有设备统计
                return getAllDevicesStatistics(statType, startTime, endTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个设备的统计数据
     */
    private ApiResponse<Map<String, Object>> getSingleDeviceStatistics(String vid, String statType, 
                                                                       LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> statistics = dataService.getDeviceDataStatistics(vid, startTime, endTime);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取单个设备统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有设备的统计数据
     */
    private ApiResponse<Map<String, Object>> getAllDevicesStatistics(String statType, 
                                                                     LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 传递null作为vid表示查询所有设备
            Map<String, Object> statistics = dataService.getDeviceDataStatistics(null, startTime, endTime);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取所有设备统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 仪表盘统计信息接口
     * 
     * 路径: GET /api/dashboard/statistics
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "onlineDevices": 15,
     *     "totalDevices": 50,
     *     "todayData": 1200,
     *     "dataGrowth": 12.5,
     *     "unresolvedAlarms": 3,
     *     "todayAlarms": 8,
     *     "alarmCount": 25,
     *     "alarmTrend": 2,
     *     "systemStatus": "normal",
     *     "cpuUsage": 45.2,
     *     "deviceStatusDistribution": {
     *       "online": 15,
     *       "offline": 30,
     *       "fault": 3,
     *       "maintenance": 2
     *     },
     *     "recentAlarms": [
     *       {
     *         "id": 1,
     *         "deviceName": "设备A",
     *         "alarmType": "温度过高",
     *         "alarmLevel": "high",
     *         "timestamp": "2023-12-01 10:30:45",
     *         "status": "pending"
     *       }
     *     ]
     *   }
     * }
     * 
     * @return ApiResponse<Map<String, Object>> 包含仪表盘统计信息的响应对象
     * @author donghuang
     * @since 2026
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        try {
            return dashboardService.getStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取仪表盘统计信息失败: " + e.getMessage());
        }
    }
}