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
        
        // 报警统计
        long totalAlarms = alarmRepository.count();
        long activeAlarms = alarmRepository.findByStatus("active").size();
        statistics.put("totalAlarms", totalAlarms);
        statistics.put("activeAlarms", activeAlarms);
        
        return ApiResponse.success(statistics);
    }

    @Override
    public ApiResponse<Map<String, Object>> getDeviceStatusDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        // 统计各种状态的设备数量
        // 这里需要通过自定义查询来获取，为简化，我们使用模拟数据
        // 在实际实现中，需要在DeviceRepository中添加相应的方法
        
        return ApiResponse.success(distribution);
    }
}