package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.dto.PaginatedResponse;
import com.iot.fresh.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 为前端兼容提供的设备控制器（单数形式）
 */
@RestController
@RequestMapping("/api/device")
public class DeviceAliasController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/list")
    public ApiResponse<PaginatedResponse<DeviceDto>> getDevices(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("Debug - DeviceAliasController.getDevices called with pageNum: " + pageNum + ", pageSize: " + pageSize);
        return deviceService.getDevicesWithPagination(pageNum, pageSize);
    }
}