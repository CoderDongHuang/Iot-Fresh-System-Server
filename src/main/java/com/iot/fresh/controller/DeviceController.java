package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.service.DeviceService;
import com.iot.fresh.service.DataService;
import com.iot.fresh.service.DeviceManagementService;
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
    
    @Autowired
    private DeviceManagementService deviceManagementService;
    
    /**
     * 新增设备
     * POST http://localhost:8080/api/device/add
     */
    @PostMapping("/add")
    public ApiResponse<Map<String, Object>> addDevice(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("接收到新增设备请求 - 请求数据: {}", requestData);
            
            // 转换前端数据为DeviceDto
            DeviceDto deviceDto = new DeviceDto();
            deviceDto.setVid((String) requestData.get("vid"));
            deviceDto.setDeviceName((String) requestData.get("deviceName"));
            deviceDto.setDeviceType((String) requestData.get("deviceType"));
            deviceDto.setLocation((String) requestData.get("location"));
            deviceDto.setDescription((String) requestData.get("description"));
            deviceDto.setManufacturer((String) requestData.get("manufacturer"));
            deviceDto.setModel((String) requestData.get("model"));
            deviceDto.setFirmwareVersion((String) requestData.get("firmwareVersion"));
            deviceDto.setContactPhone((String) requestData.get("contactPhone"));
            
            // 处理状态字段转换
            Object statusObj = requestData.get("status");
            if (statusObj != null) {
                if (statusObj instanceof String) {
                    String statusStr = (String) statusObj;
                    switch (statusStr.toLowerCase()) {
                        case "online":
                            deviceDto.setStatus(1);
                            break;
                        case "offline":
                            deviceDto.setStatus(0);
                            break;
                        case "error":
                            deviceDto.setStatus(2);
                            break;
                        case "maintenance":
                            deviceDto.setStatus(3);
                            break;
                        default:
                            try {
                                deviceDto.setStatus(Integer.parseInt(statusStr));
                            } catch (NumberFormatException e) {
                                deviceDto.setStatus(1); // 默认在线状态
                            }
                            break;
                    }
                } else if (statusObj instanceof Integer) {
                    deviceDto.setStatus((Integer) statusObj);
                } else {
                    deviceDto.setStatus(1); // 默认在线状态
                }
            } else {
                deviceDto.setStatus(1); // 默认在线状态
            }
            
            log.info("转换后的设备数据 - VID: {}, 设备名称: {}, 状态: {}", 
                deviceDto.getVid(), deviceDto.getDeviceName(), deviceDto.getStatus());
            
            // 调用服务层新增设备
            ApiResponse response = deviceService.addDevice(deviceDto);
            
            if (response.isSuccess() && response.getData() != null) {
                // 获取新增的设备信息
                DeviceDto newDevice = (DeviceDto) response.getData();
                
                // 转换为前端期望的完整格式
                Map<String, Object> deviceData = new java.util.HashMap<>();
                deviceData.put("id", newDevice.getId());
                deviceData.put("deviceName", newDevice.getDeviceName());
                deviceData.put("vid", newDevice.getVid());
                deviceData.put("deviceType", newDevice.getDeviceType());
                deviceData.put("location", newDevice.getLocation());
                deviceData.put("manufacturer", newDevice.getManufacturer());
                deviceData.put("model", newDevice.getModel());
                deviceData.put("firmwareVersion", newDevice.getFirmwareVersion());
                deviceData.put("ipAddress", newDevice.getContactPhone()); // 使用contactPhone作为ipAddress
                deviceData.put("macAddress", null); // MAC地址暂时设为null
                deviceData.put("contactPhone", newDevice.getContactPhone());
                deviceData.put("status", newDevice.getStatus());
                deviceData.put("description", newDevice.getDescription());
                
                // 格式化创建时间
                if (newDevice.getCreatedAt() != null) {
                    deviceData.put("createTime", newDevice.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    deviceData.put("createTime", null);
                }
                
                log.info("新增设备请求处理完成 - 设备ID: {}, VID: {}", newDevice.getId(), newDevice.getVid());
                return ApiResponse.success("success", deviceData);
            } else {
                return ApiResponse.error(response.getCode(), response.getMessage());
            }
            
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
     * 获取设备详情信息 - 返回前端期望的详细格式
     * GET http://localhost:8080/api/device/detail/{vid}
     */
    @GetMapping("/detail/{vid}")
    public ApiResponse<Map<String, Object>> getDeviceDetail(@PathVariable String vid) {
        try {
            log.info("接收到获取设备详情请求 - VID: {}", vid);
            
            ApiResponse response = deviceService.getDeviceByVid(vid);
            
            if (!response.isSuccess()) {
                return ApiResponse.error("设备不存在: " + vid);
            }
            
            DeviceDto device = (DeviceDto) response.getData();
            
            // 转换为前端期望的下划线格式
            Map<String, Object> deviceDetail = new java.util.HashMap<>();
            deviceDetail.put("vid", device.getVid());
            deviceDetail.put("device_name", device.getDeviceName());
            deviceDetail.put("device_type", device.getDeviceType());
            deviceDetail.put("status", device.getStatus());
            deviceDetail.put("location", device.getLocation());
            deviceDetail.put("description", device.getDescription());
            deviceDetail.put("manufacturer", device.getManufacturer());
            deviceDetail.put("model", device.getModel());
            deviceDetail.put("firmware_version", device.getFirmwareVersion());
            
            // 添加IP地址和MAC地址（如果存在）
            if (device.getContactPhone() != null) {
                deviceDetail.put("ip_address", device.getContactPhone());
            } else {
                deviceDetail.put("ip_address", null);
            }
            
            // MAC地址暂时设置为null，如果需要可以从其他字段获取
            deviceDetail.put("mac_address", null);
            
            if (device.getLastHeartbeat() != null) {
                deviceDetail.put("last_online_time", device.getLastHeartbeat().toString());
            } else {
                deviceDetail.put("last_online_time", null);
            }
            
            if (device.getCreatedAt() != null) {
                deviceDetail.put("createTime", device.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                deviceDetail.put("createTime", null);
            }
            
            log.info("获取设备详情请求处理完成 - VID: {}", vid);
            return ApiResponse.success("操作成功", deviceDetail);
            
        } catch (Exception e) {
            log.error("处理获取设备详情请求时发生错误 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取设备历史数据 - 前端期望的路径格式
     * GET http://localhost:8080/api/device/{vid}/history-data
     */
    @GetMapping("/{vid}/history-data")
    public ApiResponse<com.iot.fresh.dto.PaginatedResponse<com.iot.fresh.dto.DeviceDataDto>> getDeviceHistoryData(@PathVariable String vid,
                                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                                       @RequestParam(defaultValue = "20") Integer pageSize,
                                                                       @RequestParam(required = false) String dataType,
                                                                       @RequestParam(required = false) String startTime,
                                                                       @RequestParam(required = false) String endTime) {
        try {
            log.info("接收到获取设备历史数据请求 - VID: {}, pageNum: {}, pageSize: {}, dataType: {}, startTime: {}, endTime: {}", 
                    vid, pageNum, pageSize, dataType, startTime, endTime);
            
            // 解析时间参数
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;
            
            if (startTime != null && !startTime.trim().isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startTime, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            if (endTime != null && !endTime.trim().isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endTime, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            // 设置默认时间范围（如果未提供时间参数，查询所有数据）
            if (startDateTime == null) {
                startDateTime = java.time.LocalDateTime.of(2020, 1, 1, 0, 0); // 默认从2020年开始
            }
            
            if (endDateTime == null) {
                endDateTime = java.time.LocalDateTime.now().plusDays(1); // 默认到明天
            }
            
            log.info("查询时间范围 - 开始时间: {}, 结束时间: {}", startDateTime, endDateTime);
            
            // 调用数据服务获取历史数据
            ApiResponse<com.iot.fresh.dto.PaginatedResponse<com.iot.fresh.dto.DeviceDataDto>> response = 
                dataService.getDeviceHistoryDataWithPagination(vid, dataType, startDateTime, endDateTime, pageNum, pageSize);
            
            log.info("获取设备历史数据请求处理完成 - VID: {}, 结果: {}, 数据数量: {}", 
                    vid, response.isSuccess(), 
                    response.isSuccess() && response.getData() != null ? response.getData().getList().size() : 0);
            return response;
            
        } catch (Exception e) {
            log.error("处理获取设备历史数据请求时发生错误 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有设备列表 - 返回前端期望的标准格式
     * GET http://localhost:8080/api/device/list
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getAllDevices(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        try {
            log.info("接收到获取设备列表请求 - pageNum: {}, pageSize: {}, keyword: {}, status: {}", 
                    pageNum, pageSize, keyword, status);
            
            // 将字符串状态转换为整数状态
            Integer statusInt = null;
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "online":
                        statusInt = 1;
                        break;
                    case "offline":
                        statusInt = 0;
                        break;
                    case "error":
                        statusInt = 2;
                        break;
                    case "maintenance":
                        statusInt = 3;
                        break;
                    default:
                        try {
                            statusInt = Integer.parseInt(status);
                        } catch (NumberFormatException e) {
                            // 如果无法解析为整数，则忽略状态过滤
                            statusInt = null;
                        }
                        break;
                }
            }
            
            // 调用设备管理服务获取分页数据
            ApiResponse<com.iot.fresh.dto.PaginatedResponse<DeviceDto>> response = 
                deviceManagementService.getDeviceList(pageNum, pageSize, keyword, statusInt);
            
            log.info("设备管理服务调用结果 - 成功: {}, 数据: {}", response.isSuccess(), response.getData() != null);
            
            // 转换为前端期望的格式 - 返回所有可能用到的字段
            if (response.isSuccess() && response.getData() != null) {
                com.iot.fresh.dto.PaginatedResponse<DeviceDto> paginatedData = response.getData();
                log.info("分页数据 - 总数: {}, 当前页数量: {}", paginatedData.getTotal(), paginatedData.getList().size());
                
                // 转换为前端期望的格式 - 返回所有字段，前端按需选择
                List<Map<String, Object>> formattedList = new java.util.ArrayList<>();
                for (DeviceDto device : paginatedData.getList()) {
                    Map<String, Object> deviceMap = new java.util.HashMap<>();
                    
                    // 必需字段
                    deviceMap.put("vid", device.getVid());
                    deviceMap.put("deviceName", device.getDeviceName());
                    deviceMap.put("status", device.getStatus());
                    
                    // 时间字段（确保包含lastHeartbeat）
                    if (device.getLastHeartbeat() != null) {
                        deviceMap.put("lastHeartbeat", device.getLastHeartbeat().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        deviceMap.put("lastOnlineTime", device.getLastHeartbeat().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else {
                        deviceMap.put("lastHeartbeat", null);
                        deviceMap.put("lastOnlineTime", null);
                    }
                    
                    // 可选字段
                    deviceMap.put("contactPhone", device.getContactPhone());
                    deviceMap.put("description", device.getDescription());
                    deviceMap.put("location", device.getLocation());
                    deviceMap.put("deviceType", device.getDeviceType());
                    deviceMap.put("manufacturer", device.getManufacturer());
                    deviceMap.put("model", device.getModel());
                    deviceMap.put("firmwareVersion", device.getFirmwareVersion());
                    
                    formattedList.add(deviceMap);
                }
                
                Map<String, Object> frontendData = new java.util.HashMap<>();
                frontendData.put("list", formattedList);
                frontendData.put("total", paginatedData.getTotal());
                frontendData.put("pageNum", paginatedData.getPageNum());
                frontendData.put("pageSize", paginatedData.getPageSize());
                
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
            map.put("lxout", deviceDataDto.getLxout());
            map.put("vStatus", deviceDataDto.getVstatus());
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