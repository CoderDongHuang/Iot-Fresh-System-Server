package com.iot.fresh.controller;

import com.iot.fresh.entity.Alarm;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private AlarmRepository alarmRepository;

    // 测试直接查询所有报警
    @GetMapping("/test-all-alarms")
    public ApiResponse<Object> testAllAlarms() {
        try {
            List<Alarm> allAlarms = alarmRepository.findAll();
            System.out.println("数据库中报警总数: " + allAlarms.size());
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", allAlarms.size());
            result.put("alarms", allAlarms);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    // 测试按状态查询
    @GetMapping("/test-status/{status}")
    public ApiResponse<Object> testStatus(@PathVariable String status) {
        try {
            // 先转换API状态到数据库状态
            String dbStatus = convertApiStatusToDbStatus(status);
            List<Alarm> alarms = alarmRepository.findByStatus(dbStatus);
            System.out.println("按状态 '" + status + "' ('" + dbStatus + "') 查询到的报警数量: " + alarms.size());
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", alarms.size());
            result.put("status", status);
            result.put("dbStatus", dbStatus);
            result.put("alarms", alarms);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    // 将API状态值转换为数据库状态值
    private String convertApiStatusToDbStatus(String apiStatus) {
        if (apiStatus == null) {
            return null;
        }
        
        switch (apiStatus.toLowerCase()) {
            case "pending":
                return "active"; // API的pending对应数据库的active
            case "resolved":
                return "resolved";
            case "ignored":
                return "ignored";
            default:
                return apiStatus; // 如果不是标准状态，直接返回原值
        }
    }
}