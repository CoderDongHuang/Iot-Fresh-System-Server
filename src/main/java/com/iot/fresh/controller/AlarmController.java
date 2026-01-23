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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        return alarmService.getAlarmList(pageNum, pageSize, level, status, keyword, startDate, endDate);
    }

    // 2. 处理报警接口
    @PostMapping("/resolve/{alarmId}")
    public ApiResponse<String> resolveAlarm(@PathVariable Long alarmId) {
        return alarmService.resolveAlarm(alarmId);
    }

    // 3. 关闭报警接口
    @PostMapping("/close/{alarmId}")
    public ApiResponse<String> closeAlarm(@PathVariable Long alarmId) {
        return alarmService.closeAlarm(alarmId);
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

    // 7. 获取报警处理记录接口
    @GetMapping("/history/{alarmId}")
    public ApiResponse<Map<String, Object>> getAlarmHistory(@PathVariable Long alarmId) {
        return alarmService.getAlarmHistory(alarmId);
    }

    // 8. 添加处理记录接口
    @PostMapping("/history/{alarmId}")
    public ApiResponse<String> addAlarmHistory(
            @PathVariable Long alarmId,
            @RequestBody Map<String, String> historyData) {
        
        String action = historyData.get("action");
        String operator = historyData.get("operator");
        String remark = historyData.get("remark");
        
        return alarmService.addAlarmHistory(alarmId, action, operator, remark);
    }
}