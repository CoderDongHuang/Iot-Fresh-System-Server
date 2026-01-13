package com.iot.fresh.controller;

import com.iot.fresh.entity.Alarm;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private AlarmRepository alarmRepository;

    // 获取所有报警数据用于调试
    @GetMapping("/alarms")
    public ApiResponse<List<Alarm>> getAllAlarms() {
        List<Alarm> alarms = alarmRepository.findAll();
        return ApiResponse.success(alarms);
    }

    // 获取分页报警数据用于调试
    @GetMapping("/alarms/paged")
    public ApiResponse<Page<Alarm>> getPagedAlarms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Alarm> alarms = alarmRepository.findAll(pageable);
        return ApiResponse.success(alarms);
    }

    // 测试特定查询条件
    @GetMapping("/alarms/test-query")
    public ApiResponse<Object> testQuery(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        
        System.out.println("测试查询参数 - level: " + level + ", status: " + status + ", keyword: " + keyword);
        
        // 直接调用服务层的查询方法
        try {
            // 这里模拟调用服务层方法
            Object result = testAlarmQuery(level, status, keyword);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询出错: " + e.getMessage());
        }
    }

    // 简单的查询测试方法
    private Object testAlarmQuery(String level, String status, String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            if (level != null && !level.isEmpty() && status != null && !status.isEmpty()) {
                return alarmRepository.findByMessageContainingAndAlarmLevelAndStatusAndDeviceNameContaining(keyword, level, status, keyword, PageRequest.of(0, 10));
            } else if (level != null && !level.isEmpty()) {
                return alarmRepository.findByMessageContainingAndAlarmLevelAndDeviceNameContaining(keyword, level, keyword, PageRequest.of(0, 10));
            } else if (status != null && !status.isEmpty()) {
                return alarmRepository.findByMessageContainingAndStatusAndDeviceNameContaining(keyword, status, keyword, PageRequest.of(0, 10));
            } else {
                return alarmRepository.findByMessageContainingOrDeviceNameContaining(keyword, keyword, PageRequest.of(0, 10));
            }
        } else {
            if (level != null && !level.isEmpty() && status != null && !status.isEmpty()) {
                return alarmRepository.findByAlarmLevelAndStatus(level, status, PageRequest.of(0, 10));
            } else if (level != null && !level.isEmpty()) {
                return alarmRepository.findByAlarmLevel(level, PageRequest.of(0, 10));
            } else if (status != null && !status.isEmpty()) {
                return alarmRepository.findByStatus(status, PageRequest.of(0, 10));
            } else {
                return alarmRepository.findAll(PageRequest.of(0, 10));
            }
        }
    }
}