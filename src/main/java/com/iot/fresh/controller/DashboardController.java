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
     * 顶部统计数据接口
     * 
     * 路径: GET /api/dashboard/statistics
     * 
     * 前端期望直接返回统计对象，而不是包装的 {code, msg, data} 格式：
     * {
     *   "onlineDevices": 25,           // 设备表获取，1为在线 在线设备数量
     *   "totalDevices": 31,            // 同上 总设备数量
     *   "todayData": 156,              // 从数据表获取 今日数据量
     *   "dataGrowth": 12,              // 同上 数据增长率(%)
     *   "unresolvedAlarms": 8,         // 从报警表获取 未处理报警数量
     *   "todayAlarms": 3,              // 同上 今日新增报警
     *   "systemStatus": "正常",         // 系统状态
     *   "cpuUsage": 45,                // CPU使用率(%)
     *   "deviceStatusDistribution": {  // 设备状态分布
     *     "online": 25,                // 在线设备数量
     *     "offline": 4,                // 离线设备数量   
     *     "fault": 2                   // 故障设备数量
     *   }
     * }
     * 
     * @return Map<String, Object> 直接返回统计对象
     * @author donghuang
     * @since 2026
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        try {
            // 获取设备统计信息
            ApiResponse<Map<String, Object>> deviceStatsResponse = deviceService.getDeviceStatistics();
            Map<String, Object> deviceStats = deviceStatsResponse.isSuccess() ? deviceStatsResponse.getData() : new HashMap<>();
            
            // 获取数据统计信息
            ApiResponse<Map<String, Object>> dataStatsResponse = dataService.getDataStatistics();
            Map<String, Object> dataStats = dataStatsResponse.isSuccess() ? dataStatsResponse.getData() : new HashMap<>();
            
            // 获取报警统计信息
            ApiResponse<Map<String, Object>> alarmStatsResponse = dashboardService.getAlarmStatistics();
            Map<String, Object> alarmStats = alarmStatsResponse.isSuccess() ? alarmStatsResponse.getData() : new HashMap<>();
            
            // 构建统计对象
            Map<String, Object> statistics = new HashMap<>();
            
            // 设备统计
            statistics.put("onlineDevices", deviceStats.getOrDefault("online", 0));
            statistics.put("totalDevices", deviceStats.getOrDefault("total", 0));
            
            // 数据统计
            statistics.put("todayData", dataStats.getOrDefault("todayData", 0));
            statistics.put("dataGrowth", dataStats.getOrDefault("dataGrowth", 0));
            
            // 报警统计
            statistics.put("unresolvedAlarms", alarmStats.getOrDefault("unresolved", 0));
            statistics.put("todayAlarms", alarmStats.getOrDefault("todayAlarms", 0));
            
            // 系统状态（模拟数据）
            statistics.put("systemStatus", "正常");
            statistics.put("cpuUsage", 45);
            
            // 设备状态分布
            Map<String, Object> statusDistribution = new HashMap<>();
            statusDistribution.put("online", deviceStats.getOrDefault("online", 0));
            statusDistribution.put("offline", deviceStats.getOrDefault("offline", 0));
            statusDistribution.put("fault", deviceStats.getOrDefault("fault", 0));
            statusDistribution.put("maintenance", deviceStats.getOrDefault("maintenance", 0));
            statistics.put("deviceStatusDistribution", statusDistribution);
            
            return statistics;
            
        } catch (Exception e) {
            e.printStackTrace();
            // 返回空统计对象而不是错误响应
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("onlineDevices", 0);
            errorStats.put("totalDevices", 0);
            errorStats.put("todayData", 0);
            errorStats.put("dataGrowth", 0);
            errorStats.put("unresolvedAlarms", 0);
            errorStats.put("todayAlarms", 0);
            errorStats.put("systemStatus", "异常");
            errorStats.put("cpuUsage", 0);
            
            Map<String, Object> errorDistribution = new HashMap<>();
            errorDistribution.put("online", 0);
            errorDistribution.put("offline", 0);
            errorDistribution.put("fault", 0);
            errorDistribution.put("maintenance", 0);
            errorStats.put("deviceStatusDistribution", errorDistribution);
            
            return errorStats;
        }
    }

    /**
     * 设备状态分布图接口
     * 
     * 路径: GET /api/dashboard/statistics
     * 
     * 响应格式:
     * {
     *   "code": 200,
     *   "msg": "获取成功",
     *   "data": {
     *     "deviceStatusDistribution": {
     *       "online": 25,
     *       "offline": 4, 
     *       "fault": 2,
     *       "maintenance": 1
     *     }
     *   }
     * }
     */
    @GetMapping("/device-status-distribution")
    public ApiResponse<Map<String, Object>> getDeviceStatusDistribution() {
        try {
            Map<String, Object> responseData = new HashMap<>();
            
            // 获取设备状态分布数据
            ApiResponse<Map<String, Object>> distributionResponse = dashboardService.getDeviceStatusDistribution();
            
            if (!distributionResponse.isSuccess()) {
                return ApiResponse.error("获取设备状态分布失败: " + distributionResponse.getMessage());
            }
            
            Map<String, Object> distributionData = distributionResponse.getData();
            
            // 按照要求格式构建响应
            Map<String, Object> statusDistribution = new HashMap<>();
            statusDistribution.put("online", distributionData.get("online"));
            statusDistribution.put("offline", distributionData.get("offline"));
            statusDistribution.put("fault", distributionData.get("fault"));
            statusDistribution.put("maintenance", distributionData.get("maintenance"));
            
            responseData.put("deviceStatusDistribution", statusDistribution);
            
            return ApiResponse.success("获取成功", responseData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取设备状态分布失败: " + e.getMessage());
        }
    }
}