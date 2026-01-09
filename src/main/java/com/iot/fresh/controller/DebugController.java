package com.iot.fresh.controller;

import com.iot.fresh.entity.Device;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @GetMapping("/check-data")
    public ApiResponse<String> checkData() {
        List<Device> devices = deviceRepository.findAll();
        String deviceInfo = devices.isEmpty() ? 
            "设备表为空，没有数据" : 
            "设备总数: " + devices.size() + 
            ", 设备VIDs: " + devices.stream()
                                  .map(Device::getVid)
                                  .collect(Collectors.joining(", "));
        
        return ApiResponse.success(deviceInfo);
    }
}