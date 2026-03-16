package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;
import com.iot.fresh.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    /**
     * 报警类型统计图接口
     * 
     * 路径: GET /api/alarm/statistics
     * 
     * 响应格式:
     * {
     *   "code": 200,
     *   "msg": "获取成功",
     *   "data": {
     *     "high": 3,
     *     "medium": 8,
     *     "low": 5
     *   }
     * }
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getAlarmStatistics() {
        try {
            // 获取完整的报警统计数据
            ApiResponse<Map<String, Object>> fullStatsResponse = alarmService.getAlarmStatistics();
            
            if (!fullStatsResponse.isSuccess()) {
                return ApiResponse.error("获取报警统计数据失败: " + fullStatsResponse.getMessage());
            }
            
            Map<String, Object> fullStats = fullStatsResponse.getData();
            
            // 按照要求格式提取按级别统计的数据
            Map<String, Object> levelStats = new HashMap<>();
            
            // 紧急报警数量 (对应后端的urgent字段)
            levelStats.put("high", fullStats.get("urgent") != null ? fullStats.get("urgent") : 0);
            
            // 重要报警数量 (对应后端的重要字段)
            levelStats.put("medium", fullStats.get("important") != null ? fullStats.get("important") : 0);
            
            // 一般报警数量 (对应后端的一般字段)
            levelStats.put("low", fullStats.get("normal") != null ? fullStats.get("normal") : 0);
            
            return ApiResponse.success("获取成功", levelStats);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取报警统计数据失败: " + e.getMessage());
        }
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