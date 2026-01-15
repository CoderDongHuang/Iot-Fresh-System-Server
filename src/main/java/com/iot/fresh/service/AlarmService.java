package com.iot.fresh.service;

import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;
import java.util.Map;

/**
 * 报警服务接口
 * 提供报警管理相关功能
 * 
 * @author donghuang
 * @since 2026
 */
public interface AlarmService {
    /**
     * 获取报警列表（支持分页、筛选）
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param level 报警级别
     * @param status 报警状态
     * @param keyword 关键词搜索
     * @return ApiResponse<PaginatedResponse<Map<String, Object>>> 报警列表响应对象
     */
    ApiResponse<PaginatedResponse<Map<String, Object>>> getAlarmList(Integer pageNum, Integer pageSize, String level, String status, String keyword);
    
    /**
     * 解决报警
     * 
     * @param alarmId 报警ID
     * @return ApiResponse<String> 操作结果响应对象
     */
    ApiResponse<String> resolveAlarm(Long alarmId);
    
    /**
     * 忽略报警
     * 
     * @param alarmId 报警ID
     * @return ApiResponse<String> 操作结果响应对象
     */
    ApiResponse<String> ignoreAlarm(Long alarmId);
    
    /**
     * 清除所有报警
     * 
     * @return ApiResponse<String> 操作结果响应对象
     */
    ApiResponse<String> clearAllAlarms();
    
    /**
     * 获取报警详情
     * 
     * @param alarmId 报警ID
     * @return ApiResponse<Map<String, Object>> 报警详情响应对象
     */
    ApiResponse<Map<String, Object>> getAlarmDetail(Long alarmId);
    
    /**
     * 获取报警统计信息
     * 
     * @return ApiResponse<Map<String, Object>> 报警统计响应对象
     */
    ApiResponse<Map<String, Object>> getAlarmStatistics();
    
    /**
     * 处理报警数据
     * 
     * @param alarmData 报警数据传输对象
     */
    void processAlarm(AlarmDataDto alarmData);
    
    /**
     * 创建报警
     * 
     * @param alarmDto 报警数据传输对象
     */
    void createAlarm(AlarmDto alarmDto);
}