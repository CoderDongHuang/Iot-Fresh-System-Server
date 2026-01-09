package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;
import com.iot.fresh.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    // 1. 获取报警列表接口
    @GetMapping("/list")
    public ApiResponse<PaginatedResponse<Map<String, Object>>> getAlarmList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        
        return alarmService.getAlarmList(pageNum, pageSize, level, status, keyword);
    }

    // 2. 处理报警接口
    @PostMapping("/resolve/{alarmId}")
    public ApiResponse<String> resolveAlarm(@PathVariable Long alarmId) {
        return alarmService.resolveAlarm(alarmId);
    }

    // 3. 忽略报警接口
    @PostMapping("/ignore/{alarmId}")
    public ApiResponse<String> ignoreAlarm(@PathVariable Long alarmId) {
        return alarmService.ignoreAlarm(alarmId);
    }

    // 4. 清除全部报警接口
    @DeleteMapping("/clear-all")
    public ApiResponse<String> clearAllAlarms() {
        return alarmService.clearAllAlarms();
    }

    // 5. 获取报警详情接口
    @GetMapping("/detail/{alarmId}")
    public ApiResponse<Map<String, Object>> getAlarmDetail(@PathVariable Long alarmId) {
        return alarmService.getAlarmDetail(alarmId);
    }

    // 6. 获取报警统计接口
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getAlarmStatistics() {
        return alarmService.getAlarmStatistics();
    }
}