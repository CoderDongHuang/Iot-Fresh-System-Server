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
        
        // 真实的设备状态统计
        long onlineDevices = deviceRepository.countByStatus(1); // 状态1表示在线
        long offlineDevices = deviceRepository.countByStatus(0); // 状态0表示离线
        long faultDevices = deviceRepository.countByStatus(2); // 状态2表示故障
        long maintenanceDevices = deviceRepository.countByStatus(3); // 状态3表示维护
        long totalDevices = deviceRepository.count();
        
        // 顶部统计数据 - 按照要求格式
        statistics.put("onlineDevices", onlineDevices);
        statistics.put("totalDevices", totalDevices);
        
        // 今日数据（模拟数据）- 使用更合理的数值
        statistics.put("todayData", 156);
        
        // 数据增长率（模拟数据）- 使用整数百分比
        statistics.put("dataGrowth", 12);
        
        // 未解决报警 (active状态对应pending)
        long unresolvedAlarms = alarmRepository.findByStatus("active").size();
        statistics.put("unresolvedAlarms", unresolvedAlarms);
        
        // 今日新增报警（模拟数据）- 使用更合理的数值
        statistics.put("todayAlarms", 3);
        
        // 系统状态（模拟数据）
        statistics.put("systemStatus", "正常");
        
        // CPU使用率（模拟数据）
        statistics.put("cpuUsage", 45);
        
        // 设备状态分布 - 按照要求格式（只包含online、offline、fault）
        Map<String, Object> deviceStatusDistribution = new HashMap<>();
        deviceStatusDistribution.put("online", onlineDevices);
        deviceStatusDistribution.put("offline", offlineDevices);
        deviceStatusDistribution.put("fault", faultDevices);
        statistics.put("deviceStatusDistribution", deviceStatusDistribution);
        
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
    
    @Override
    public ApiResponse<Map<String, Object>> getAlarmStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取未处理报警数量（状态为active）
            long unresolvedAlarms = alarmRepository.findByStatus("active").size();
            statistics.put("unresolved", unresolvedAlarms);
            
            // 获取今日新增报警数量（创建时间为今天）
            long todayAlarms = alarmRepository.countTodayAlarms();
            statistics.put("todayAlarms", todayAlarms);
            
            return ApiResponse.success("获取报警统计信息成功", statistics);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取报警统计信息失败: " + e.getMessage());
        }
    }
}