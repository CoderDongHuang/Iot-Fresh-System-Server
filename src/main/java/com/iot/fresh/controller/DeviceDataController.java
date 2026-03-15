package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.entity.DeviceDataHistory;
import com.iot.fresh.service.DataService;
import com.iot.fresh.service.DeviceDataHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device/data")
public class DeviceDataController {

    @Autowired
    private DataService dataService;
    
    @Autowired
    private DeviceDataHistoryService deviceDataHistoryService;

    /**
     * 获取设备历史数据用于趋势图展示
     * 
     * 路径: GET /api/device/history-data/{vid}
     * 
     * 参数:
     * - {vid} - 路径参数，设备唯一标识符
     * - timeRange - 时间范围（可选：1h/6h/24h/7d/30d）
     * - startTime - 开始时间（ISO 8601格式，可选）
     * - endTime - 结束时间（ISO 8601格式，可选）
     * - metric - 指标类型（可选：temperature/humidity/light/all）
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": [
     *     {
     *       "timestamp": "2024-12-19T10:30:00",
     *       "tin": 25.5,
     *       "tout": 23.0,
     *       "lxin": 1200,
     *       "brightness": 80
     *     },
     *     ...
     *   ]
     * }
     * 
     * @param vid 设备唯一标识符
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param timeRange 时间范围（可选）
     * @param metric 指标类型（可选）
     * @return ApiResponse<List<Map<String, Object>>> 包含设备历史数据的响应对象
     * @author donghuang
     * @since 2026
     */
    @GetMapping("/history-data/{vid}")
    public ApiResponse<List<Map<String, Object>>> getHistoryData(@PathVariable String vid,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                                 @RequestParam(required = false) String timeRange,
                                                                 @RequestParam(required = false) String metric) {
        // 如果指定了timeRange，根据timeRange计算开始和结束时间
        if (timeRange != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (timeRange.toLowerCase()) {
                case "1h":
                    startTime = now.minusHours(1);
                    break;
                case "6h":
                    startTime = now.minusHours(6);
                    break;
                case "24h":
                    startTime = now.minusHours(24);
                    break;
                case "7d":
                    startTime = now.minusDays(7);
                    break;
                case "30d":
                    startTime = now.minusDays(30);
                    break;
                default:
                    // 如果timeRange无效，使用默认的24小时
                    startTime = now.minusHours(24);
                    break;
            }
            if (endTime == null) {
                endTime = now;
            }
        } else {
            // 如果没有指定timeRange，使用默认的30天
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
        }

        System.out.println("获取设备历史数据 - VID: " + vid + ", 时间范围: " + startTime + " 到 " + endTime);

        // 获取设备历史数据
        ApiResponse<List<DeviceDataDto>> response = dataService.getDeviceHistoryData(vid, startTime, endTime);
        
        if (response.isSuccess()) {
            List<DeviceDataDto> deviceDataList = response.getData();
            System.out.println("查询到 " + deviceDataList.size() + " 条设备数据");

            // 将DeviceDataDto列表转换为前端期望的格式
            List<Map<String, Object>> result = deviceDataList.stream()
            .filter(data -> data.getTimestamp() != null || data.getCreatedAt() != null) // 过滤掉没有时间戳的数据
            .map(data -> {
                Map<String, Object> item = new HashMap<>();
                
                // 使用ISO 8601格式的时间字符串
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                item.put("timestamp", data.getTimestamp() != null ? 
                    data.getTimestamp().format(formatter) : 
                    data.getCreatedAt().format(formatter));
                
                // 根据metric参数决定返回哪些字段，确保数值不为null
                if (metric == null || metric.equals("all") || metric.equals("temperature")) {
                    if (data.getTin() != null) item.put("tin", data.getTin());
                    if (data.getTout() != null) item.put("tout", data.getTout());
                }
                
                if (metric == null || metric.equals("all") || metric.equals("humidity")) {
                    if (data.getHin() != null) item.put("hin", data.getHin());
                    if (data.getHout() != null) item.put("hout", data.getHout());
                }
                
                if (metric == null || metric.equals("all") || metric.equals("light")) {
                    if (data.getLxin() != null) item.put("lxin", data.getLxin());
                    if (data.getBrightness() != null) item.put("brightness", data.getBrightness());
                }
                
                // 添加表格所需的额外字段
                if (data.getVstatus() != null) item.put("vStatus", data.getVstatus());
                // battery和light字段已移除
                
                return item;
            })
            .sorted((a, b) -> {
                String timeA = (String) a.get("timestamp");
                String timeB = (String) b.get("timestamp");
                if (timeA == null || timeB == null) {
                    return 0; // 如果任一时间戳为null，视为相等
                }
                return timeA.compareTo(timeB);
            })
            .collect(Collectors.toList());
            
            System.out.println("最终返回 " + result.size() + " 条格式化后的数据");
            
            // 如果没有数据，返回空数组而不是错误
            if (result.isEmpty()) {
                System.out.println("警告: 设备 " + vid + " 在指定时间范围内没有历史数据");
                return ApiResponse.success(new java.util.ArrayList<>());
            }
            
            return ApiResponse.success(result);
        } else {
            System.out.println("获取设备历史数据失败: " + response.getMsg());
            // 即使服务调用失败，也返回空数组而不是错误，以避免前端崩溃
            return ApiResponse.success(new java.util.ArrayList<>());
        }
    }

    /**
     * 历史数据查询接口 - 数据来源改为 device_data_history 表
     * 
     * 路径: GET /api/device/history-data
     * 
     * 请求参数:
     * - pageNum - 页码
     * - pageSize - 每页大小
     * - vid - 设备VID（可选）
     * - startTime - 开始时间（可选）
     * - endTime - 结束时间（可选）
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "list": [
     *       {
     *         "update_at": "2026-03-15 14:30:00",
     *         "tin": 25.5,
     *         "tout": 20.3,
     *         "lxin": 500,
     *         "vStatus": 0
     *       }
     *     ],
     *     "total": 50,
     *     "pageSize": 10,
     *     "pageNum": 1
     *   }
     * }
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param vid 设备VID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return ApiResponse<Map<String, Object>> 包含分页历史数据的响应对象
     * @author donghuang
     * @since 2026
     */
    @GetMapping("/history-data")
    public ApiResponse<Map<String, Object>> getHistoryDataList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String vid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String endTime) {
        
        try {
            // 转换时间格式
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                try {
                    startDateTime = LocalDateTime.parse(startTime.replace(" ", "T"));
                } catch (Exception e) {
                    try {
                        startDateTime = LocalDateTime.parse(startTime);
                    } catch (Exception ex) {
                        System.out.println("无法解析开始时间: " + startTime);
                        return ApiResponse.error("开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
                    }
                }
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                try {
                    endDateTime = LocalDateTime.parse(endTime.replace(" ", "T"));
                } catch (Exception e) {
                    try {
                        endDateTime = LocalDateTime.parse(endTime);
                    } catch (Exception ex) {
                        System.out.println("无法解析结束时间: " + endTime);
                        return ApiResponse.error("结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
                    }
                }
            }
            
            // 如果没有指定时间，默认查询最近30天的数据
            if (startDateTime == null) {
                startDateTime = LocalDateTime.now().minusDays(30);
            }
            if (endDateTime == null) {
                endDateTime = LocalDateTime.now();
            }
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            
            // 查询历史数据表
            Page<DeviceDataHistory> historyPage;
            if (vid != null && !vid.trim().isEmpty()) {
                // 按设备ID和时间范围查询
                historyPage = deviceDataHistoryService.getHistoryDataByVidAndTimeRange(vid, startDateTime, endDateTime, pageable);
            } else {
                // 按时间范围查询所有设备
                historyPage = deviceDataHistoryService.getHistoryDataByTimeRange(startDateTime, endDateTime, pageable);
            }
            
            // 转换数据格式以匹配前端期望
            List<Map<String, Object>> resultList = historyPage.getContent().stream().map(history -> {
                Map<String, Object> item = new HashMap<>();
                
                // 按照指定格式返回字段
                if (history.getUpdatedAt() != null) {
                    item.put("update_at", history.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                if (history.getTin() != null) item.put("tin", history.getTin());
                if (history.getTout() != null) item.put("tout", history.getTout());
                if (history.getLxin() != null) item.put("lxin", history.getLxin());
                if (history.getVstatus() != null) item.put("vStatus", history.getVstatus());
                
                return item;
            }).collect(Collectors.toList());
            
            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("list", resultList);
            result.put("total", (int) historyPage.getTotalElements());
            result.put("pageSize", pageSize);
            result.put("pageNum", pageNum);
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取历史数据失败: " + e.getMessage());
        }
    }
}