package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.service.DeviceService;
import com.iot.fresh.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {
    
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private DataService dataService;
    
    /**
     * 新增设备
     * POST http://localhost:8080/api/device/add
     */
    @PostMapping("/add")
    public ApiResponse addDevice(@RequestBody DeviceDto deviceDto) {
        try {
            log.info("接收到新增设备请求 - 设备名称: {}, VID: {}", 
                deviceDto.getDeviceName(), deviceDto.getVid());
            
            // 调用服务层新增设备
            ApiResponse response = deviceService.addDevice(deviceDto);
            
            log.info("新增设备请求处理完成 - 结果: {}", response.isSuccess());
            return response;
            
        } catch (Exception e) {
            log.error("处理新增设备请求时发生错误: {}", e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据VID获取设备信息
     * GET http://localhost:8080/api/device/{vid}
     */
    @GetMapping("/{vid}")
    public ApiResponse getDevice(@PathVariable String vid) {
        try {
            log.info("接收到查询设备请求 - VID: {}", vid);
            
            ApiResponse response = deviceService.getDeviceByVid(vid);
            
            log.info("查询设备请求处理完成 - VID: {}, 结果: {}", vid, response.isSuccess());
            return response;
            
        } catch (Exception e) {
            log.error("处理查询设备请求时发生错误 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有设备列表 - 返回前端期望的标准格式
     * GET http://localhost:8080/api/device/list
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getAllDevices() {
        try {
            log.info("接收到获取设备列表请求");
            
            ApiResponse response = deviceService.getAllDevices();
            
            // 转换为前端期望的格式
            if (response.isSuccess() && response.getData() != null) {
                List<DeviceDto> deviceList = (List<DeviceDto>) response.getData();
                
                // 转换为前端要求的下划线格式
                List<Map<String, Object>> formattedList = new java.util.ArrayList<>();
                for (DeviceDto device : deviceList) {
                    Map<String, Object> deviceMap = new java.util.HashMap<>();
                    deviceMap.put("vid", device.getVid());
                    deviceMap.put("device_name", device.getDeviceName());
                    deviceMap.put("device_type", device.getDeviceType());
                    deviceMap.put("status", device.getStatus());
                    deviceMap.put("location", device.getLocation());
                    if (device.getLastHeartbeat() != null) {
                        deviceMap.put("last_online_time", device.getLastHeartbeat().toString());
                    } else {
                        deviceMap.put("last_online_time", null);
                    }
                    formattedList.add(deviceMap);
                }
                
                Map<String, Object> frontendData = new java.util.HashMap<>();
                frontendData.put("list", formattedList);
                frontendData.put("total", formattedList.size());
                
                log.info("获取设备列表请求处理完成 - 设备数量: {}", formattedList.size());
                return ApiResponse.success("操作成功", frontendData);
            } else {
                return ApiResponse.error(response.getCode(), response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理获取设备列表请求时发生错误: {}", e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新设备信息
     * PUT http://localhost:8080/api/device/{vid}
     */
    @PutMapping("/{vid}")
    public ApiResponse updateDevice(@PathVariable String vid, @RequestBody DeviceDto deviceDto) {
        try {
            log.info("接收到更新设备请求 - VID: {}", vid);
            
            ApiResponse response = deviceService.updateDevice(vid, deviceDto);
            
            log.info("更新设备请求处理完成 - VID: {}, 结果: {}", vid, response.isSuccess());
            return response;
            
        } catch (Exception e) {
            log.error("处理更新设备请求时发生错误 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 删除设备
     * DELETE http://localhost:8080/api/device/{vid}
     */
    @DeleteMapping("/{vid}")
    public ApiResponse deleteDevice(@PathVariable String vid) {
        try {
            log.info("接收到删除设备请求 - VID: {}", vid);
            
            ApiResponse response = deviceService.deleteDevice(vid);
            
            log.info("删除设备请求处理完成 - VID: {}, 结果: {}", vid, response.isSuccess());
            return response;
            
        } catch (Exception e) {
            log.error("处理删除设备请求时发生错误 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新设备状态
     * PATCH http://localhost:8080/api/device/{vid}/status
     */
    @PatchMapping("/{vid}/status")
    public ApiResponse updateDeviceStatus(@PathVariable String vid, @RequestParam Integer status) {
        try {
            log.info("接收到更新设备状态请求 - VID: {}, 状态: {}", vid, status);
            
            ApiResponse response = deviceService.updateDeviceStatus(vid, status);
            
            log.info("更新设备状态请求处理完成 - VID: {}, 状态: {}, 结果: {}", 
                vid, status, response.isSuccess());
            return response;
            
        } catch (Exception e) {
            log.error("处理更新设备状态请求时发生错误 - VID: {}, 状态: {}, 错误: {}", 
                vid, status, e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有设备实时数据
     * GET http://localhost:8080/api/device/real-time-data
     * 直接从device_data表获取最新的设备传感器数据
     */
    @GetMapping("/real-time-data")
    public ApiResponse<List<Map<String, Object>>> getAllRealTimeData() {
        try {
            log.info("接收到获取所有设备实时数据请求");
            
            // 获取所有设备信息
            ApiResponse<List<DeviceDto>> deviceResponse = deviceService.getAllDevices();
            
            if (!deviceResponse.isSuccess()) {
                return ApiResponse.error("获取设备列表失败: " + deviceResponse.getMsg());
            }
            
            List<DeviceDto> devices = deviceResponse.getData();
            
            // 创建结果列表
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            
            // 为每个设备获取最新的传感器数据
            for (DeviceDto device : devices) {
                Map<String, Object> deviceDataMap = new java.util.HashMap<>();
                
                // 基本设备信息
                deviceDataMap.put("id", device.getId());
                deviceDataMap.put("vid", device.getVid());
                deviceDataMap.put("deviceName", device.getDeviceName());
                deviceDataMap.put("deviceType", device.getDeviceType());
                deviceDataMap.put("location", device.getLocation());
                deviceDataMap.put("status", device.getStatus());
                deviceDataMap.put("lastOnlineTime", device.getLastOnlineTime());
                deviceDataMap.put("createTime", device.getCreateTime());
                
                // 直接从device_data表获取该设备的最新传感器数据
                com.iot.fresh.entity.DeviceData latestDeviceData = dataService.getLatestDeviceData(device.getVid());
                if (latestDeviceData != null) {
                    deviceDataMap.put("tin", latestDeviceData.getTin());
                    deviceDataMap.put("tout", latestDeviceData.getTout());
                    deviceDataMap.put("hin", latestDeviceData.getHin());
                    deviceDataMap.put("hout", latestDeviceData.getHout());
                    deviceDataMap.put("lxin", latestDeviceData.getLxin());
                    deviceDataMap.put("lxout", latestDeviceData.getLxout());
                    deviceDataMap.put("brightness", latestDeviceData.getBrightness());
                    deviceDataMap.put("vStatus", latestDeviceData.getVstatus());
                    
                    if (latestDeviceData.getTimestamp() != null) {
                        deviceDataMap.put("timestamp", latestDeviceData.getTimestamp().toString());
                    } else if (latestDeviceData.getCreatedAt() != null) {
                        deviceDataMap.put("timestamp", latestDeviceData.getCreatedAt().toString());
                    }
                }
                
                result.add(deviceDataMap);
            }
            
            log.info("获取所有设备实时数据请求处理完成 - 设备数量: {}", result.size());
            return ApiResponse.success("获取实时数据成功", result);
            
        } catch (Exception e) {
            log.error("处理获取所有设备实时数据请求时发生错误: {}", e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个设备实时数据
     * GET http://localhost:8080/api/device/real-time-data/{vid}
     * 直接从device_data表获取最新的设备传感器数据
     */
    @GetMapping("/real-time-data/{vid}")
    public ApiResponse<Map<String, Object>> getRealTimeData(@PathVariable String vid) {
        try {
            log.info("接收到获取设备实时数据请求 - VID: {}", vid);
            
            // 获取设备基本信息
            ApiResponse<DeviceDto> deviceResponse = deviceService.getDeviceByVid(vid);
            
            if (!deviceResponse.isSuccess()) {
                return ApiResponse.error("设备不存在: " + vid);
            }
            
            DeviceDto device = deviceResponse.getData();
            Map<String, Object> deviceDataMap = new java.util.HashMap<>();
            
            // 基本设备信息
            deviceDataMap.put("id", device.getId());
            deviceDataMap.put("vid", device.getVid());
            deviceDataMap.put("deviceName", device.getDeviceName());
            deviceDataMap.put("deviceType", device.getDeviceType());
            deviceDataMap.put("location", device.getLocation());
            deviceDataMap.put("status", device.getStatus());
            deviceDataMap.put("lastOnlineTime", device.getLastOnlineTime());
            deviceDataMap.put("createTime", device.getCreateTime());
            
            // 直接从device_data表获取该设备的最新传感器数据
            com.iot.fresh.entity.DeviceData latestDeviceData = dataService.getLatestDeviceData(vid);
            if (latestDeviceData != null) {
                deviceDataMap.put("tin", latestDeviceData.getTin());
                deviceDataMap.put("tout", latestDeviceData.getTout());
                deviceDataMap.put("hin", latestDeviceData.getHin());
                deviceDataMap.put("hout", latestDeviceData.getHout());
                deviceDataMap.put("lxin", latestDeviceData.getLxin());
                deviceDataMap.put("lxout", latestDeviceData.getLxout());
                deviceDataMap.put("brightness", latestDeviceData.getBrightness());
                deviceDataMap.put("vStatus", latestDeviceData.getVstatus());
                
                if (latestDeviceData.getTimestamp() != null) {
                    deviceDataMap.put("timestamp", latestDeviceData.getTimestamp().toString());
                } else if (latestDeviceData.getCreatedAt() != null) {
                    deviceDataMap.put("timestamp", latestDeviceData.getCreatedAt().toString());
                }
            }
            
            log.info("获取设备实时数据请求处理完成 - VID: {}", vid);
            return ApiResponse.success("获取实时数据成功", deviceDataMap);
            
        } catch (Exception e) {
            log.error("处理获取设备实时数据请求时发生错误 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 将DeviceDataDto转换为Map格式
     */
    private Map<String, Object> convertDeviceDataDtoToMap(com.iot.fresh.dto.DeviceDataDto deviceDataDto) {
        Map<String, Object> map = new java.util.HashMap<>();
        
        if (deviceDataDto != null) {
            map.put("tin", deviceDataDto.getTin());
            map.put("tout", deviceDataDto.getTout());
            map.put("hin", deviceDataDto.getHin());
            map.put("hout", deviceDataDto.getHout());
            map.put("lxin", deviceDataDto.getLxin());
            map.put("light", deviceDataDto.getLight());
            map.put("pid", deviceDataDto.getPid());
            map.put("vStatus", deviceDataDto.getVstatus());
            map.put("battery", deviceDataDto.getBattery());
            map.put("brightness", deviceDataDto.getBrightness());
            map.put("speedM1", deviceDataDto.getSpeedM1());
            map.put("speedM2", deviceDataDto.getSpeedM2());
            
            if (deviceDataDto.getTimestamp() != null) {
                map.put("timestamp", deviceDataDto.getTimestamp().toString());
            }
        }
        
        return map;
    }
}