package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据调试控制器
 * 用于诊断数据查询问题
 * 
 * @author donghuang
 * @since 2026
 */
@RestController
@RequestMapping("/api/debug")
public class DataDebugController {

    @Autowired
    private DeviceDataRepository deviceDataRepository;

    /**
     * 获取数据库中所有数据记录数量
     */
    @GetMapping("/data-count")
    public ApiResponse<Long> getTotalDataCount() {
        long count = deviceDataRepository.count();
        return ApiResponse.success(count);
    }

    /**
     * 获取特定时间段内的数据记录数量
     */
    @GetMapping("/data-count-by-time")
    public ApiResponse<Long> getDataCountByTime(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String endTime) {
        
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startTime != null && !startTime.trim().isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace(" ", "T"));
        } else {
            // 默认使用30天前
            startDateTime = LocalDateTime.now().minusDays(30);
        }
        
        if (endTime != null && !endTime.trim().isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace(" ", "T"));
        } else {
            // 默认使用当前时间
            endDateTime = LocalDateTime.now();
        }
        
        // 查询指定时间范围内的数据
        List<DeviceData> dataList = deviceDataRepository.findByTimeRangeWithNoPagination(startDateTime, endDateTime);
        long count = dataList.size();
        
        return ApiResponse.success(count);
    }

    /**
     * 获取最近的一些数据记录
     */
    @GetMapping("/recent-data")
    public ApiResponse<List<DeviceData>> getRecentData(@RequestParam(defaultValue = "10") int limit) {
        // 这里需要添加一个方法来获取最近的记录
        // 由于Repository中没有现成的方法，我们暂时返回错误提示
        return ApiResponse.error("暂不支持此功能，请联系管理员");
    }
    
    /**
     * 获取所有设备VID列表
     */
    @GetMapping("/all-device-vids")
    public ApiResponse<List<String>> getAllDeviceVids() {
        // 查询所有唯一的VID
        List<String> vids = deviceDataRepository.findDistinctVids();
        return ApiResponse.success(vids);
    }
}