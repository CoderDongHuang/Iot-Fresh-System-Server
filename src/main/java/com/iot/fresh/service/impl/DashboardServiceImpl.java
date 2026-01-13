package com.iot.fresh.service.impl;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Override
    public ApiResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 设备统计
        long totalDevices = deviceRepository.count();
        statistics.put("totalDevices", totalDevices);
        
        // 真实的设备状态统计
        long onlineDevices = deviceRepository.countByStatus(1); // 状态1表示在线
        long offlineDevices = deviceRepository.countByStatus(0); // 状态0表示离线
        long faultDevices = deviceRepository.countByStatus(2); // 状态2表示故障
        long maintenanceDevices = deviceRepository.countByStatus(3); // 状态3表示维护
        
        statistics.put("onlineDevices", onlineDevices);
        
        // 今日数据（模拟数据）
        statistics.put("todayData", 120);
        
        // 数据增长（模拟数据）
        statistics.put("dataGrowth", 5.5);
        
        // 未解决报警 (active状态对应pending)
        long unresolvedAlarms = alarmRepository.findByStatus("active").size();
        statistics.put("unresolvedAlarms", unresolvedAlarms);
        
        // 今日报警（模拟数据）
        statistics.put("todayAlarms", 8);
        
        // 报警总数
        long alarmCount = alarmRepository.count();
        statistics.put("alarmCount", alarmCount);
        
        // 报警趋势（模拟数据）
        statistics.put("alarmTrend", 3);
        
        // 系统状态（模拟数据）
        statistics.put("systemStatus", "正常");
        
        // CPU使用率（模拟数据）
        statistics.put("cpuUsage", 45);
        
        // 设备状态分布
        Map<String, Object> deviceStatusDistribution = new HashMap<>();
        deviceStatusDistribution.put("online", onlineDevices);
        deviceStatusDistribution.put("offline", offlineDevices);
        deviceStatusDistribution.put("fault", faultDevices);
        deviceStatusDistribution.put("maintenance", maintenanceDevices);
        statistics.put("deviceStatusDistribution", deviceStatusDistribution);
        
        // 最近报警（模拟数据）
        // 在实际应用中，这里应该查询最近的报警记录
        statistics.put("recentAlarms", new Object[]{}); // 空数组作为占位符
        
        return ApiResponse.success(statistics);
    }

    @Override
    public ApiResponse<Map<String, Object>> getDeviceStatusDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        // 真实的设备状态统计
        long onlineDevices = deviceRepository.countByStatus(1); // 状态1表示在线
        long offlineDevices = deviceRepository.countByStatus(0); // 状态0表示离线
        long faultDevices = deviceRepository.countByStatus(2); // 状态2表示故障
        long maintenanceDevices = deviceRepository.countByStatus(3); // 状态3表示维护
        
        distribution.put("online", onlineDevices);
        distribution.put("offline", offlineDevices);
        distribution.put("fault", faultDevices);
        distribution.put("maintenance", maintenanceDevices);
        
        return ApiResponse.success(distribution);
    }
}