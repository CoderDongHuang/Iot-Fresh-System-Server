package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device")
public class DeviceDataController {

    @Autowired
    private DataService dataService;

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
                if (data.getBattery() != null) item.put("battery", data.getBattery());
                if (data.getLight() != null) item.put("light", data.getLight());
                
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
     * 历史数据查询接口
     * 
     * 路径: GET /api/device/history-data
     * 
     * 请求参数:
     * - pageNum - 页码
     * - pageSize - 每页大小
     * - vid - 设备VID（可选）
     * - dataType - 数据类型（可选：temperature, humidity, light, other）
     * - startTime - 开始时间（可选）
     * - endTime - 结束时间（可选）
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "list": [
     *       {
     *         "id": 1,
     *         "vid": "device001",
     *         "tin": 23.5,
     *         "tout": 22.1,
     *         "lxin": 150.5,
     *         "brightness": 80,
     *         "vStatus": 1,
     *         "timestamp": "2023-12-01 10:30:45"
     *       }
     *     ],
     *     "total": 100,
     *     "pageNum": 1,
     *     "pageSize": 20
     *   }
     * }
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param vid 设备VID（可选）
     * @param dataType 数据类型（可选）
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
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String endTime) {
        
        try {
            // 转换时间格式
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                // 尝试解析不同格式的时间字符串
                try {
                    startDateTime = LocalDateTime.parse(startTime.replace(" ", "T"));
                } catch (Exception e) {
                    // 如果标准格式失败，尝试其他格式
                    try {
                        startDateTime = LocalDateTime.parse(startTime);
                    } catch (Exception ex) {
                        System.out.println("无法解析开始时间: " + startTime);
                        return ApiResponse.error("开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
                    }
                }
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                // 尝试解析不同格式的时间字符串
                try {
                    endDateTime = LocalDateTime.parse(endTime.replace(" ", "T"));
                } catch (Exception e) {
                    // 如果标准格式失败，尝试其他格式
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
            
            // 调用服务层获取分页数据
                ApiResponse<com.iot.fresh.dto.PaginatedResponse<DeviceDataDto>> response = 
                    dataService.getDeviceHistoryDataWithPagination(vid, dataType, startDateTime, endDateTime, pageNum, pageSize);
            
            if (response.isSuccess()) {
                List<DeviceDataDto> dataList = response.getData().getList();
                
                // 转换数据格式以匹配前端期望
                List<Map<String, Object>> resultList = dataList.stream().map(data -> {
                    Map<String, Object> item = new HashMap<>();
                    
                    item.put("id", data.getId());
                    item.put("vid", data.getVid());
                    if (data.getTin() != null) item.put("tin", data.getTin());
                    if (data.getTout() != null) item.put("tout", data.getTout());
                    if (data.getLxin() != null) item.put("lxin", data.getLxin());
                    if (data.getBrightness() != null) item.put("brightness", data.getBrightness());
                    if (data.getVstatus() != null) item.put("vStatus", data.getVstatus());
                    
                    // 格式化时间戳
                    if (data.getTimestamp() != null) {
                        item.put("timestamp", data.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else if (data.getCreatedAt() != null) {
                        item.put("timestamp", data.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    
                    return item;
                }).collect(java.util.stream.Collectors.toList());
                
                // 构造分页响应
                Map<String, Object> result = new HashMap<>();
                result.put("list", resultList);
                result.put("total", response.getData().getTotal());
                result.put("pageNum", pageNum);
                result.put("pageSize", pageSize);
                
                return ApiResponse.success(result);
            } else {
                // 如果服务调用失败，返回空结果
                Map<String, Object> result = new HashMap<>();
                result.put("list", java.util.Collections.emptyList());
                result.put("total", 0);
                result.put("pageNum", pageNum);
                result.put("pageSize", pageSize);
                
                return ApiResponse.success(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取历史数据失败: " + e.getMessage());
        }
    }
}