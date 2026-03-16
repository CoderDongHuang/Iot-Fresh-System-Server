package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.service.DeviceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/device")
public class DeviceControlController {

    @Autowired
    private DeviceManagementService deviceManagementService;

    /**
     * 设备控制接口 - 统一控制接口
     * 路径: POST http://localhost:8080/api/device/control/{vid}
     * 
     * 请求格式:
     * {
     *   "command": "turnOn",
     *   "params": {
     *     "temperature": 25,
     *     "brightness": 80
     *   }
     * }
     * 
     * 响应格式:
     * {
     *   "code": 200,
     *   "message": "设备控制成功",
     *   "data": {
     *     "result": "success",
     *     "deviceStatus": "online"
     *   },
     *   "success": true
     * }
     */
    @PostMapping("/control/{vid}")
    public ApiResponse<Map<String, Object>> controlDevice(
            @PathVariable String vid,
            @RequestBody Map<String, Object> controlCommand) {
        
        // 调用服务层执行设备控制
        ApiResponse<String> serviceResponse = deviceManagementService.controlDevice(vid, controlCommand);
        
        // 转换为标准响应格式
        if (serviceResponse.isSuccess()) {
            Map<String, Object> resultData = new java.util.HashMap<>();
            resultData.put("result", "success");
            resultData.put("deviceStatus", "online");
            
            return ApiResponse.success("设备控制成功", resultData);
        } else {
            // 错误响应也保持标准格式
            Map<String, Object> errorData = new java.util.HashMap<>();
            errorData.put("result", "failed");
            errorData.put("deviceStatus", "unknown");
            
            return ApiResponse.error(serviceResponse.getCode(), serviceResponse.getMessage(), errorData);
        }
    }
}