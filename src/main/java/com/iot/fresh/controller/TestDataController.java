package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 测试数据生成控制器
 * 用于生成测试数据以验证数据查询功能
 * 
 * @author donghuang
 * @since 2026
 */
@RestController
@RequestMapping("/api/test-data")
public class TestDataController {

    @Autowired
    private DeviceDataRepository deviceDataRepository;

    private Random random = new Random();

    /**
     * 生成测试数据
     */
    @PostMapping("/generate")
    public ApiResponse<String> generateTestData(
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "DEV001") String vid,
            @RequestParam(defaultValue = "设备1") String deviceName) {
        
        try {
            for (int i = 0; i < count; i++) {
                DeviceData data = new DeviceData();
                data.setVid(vid);
                data.setDeviceName(deviceName);
                data.setDeviceType("TemperatureSensor");
                
                // 生成模拟数据
                data.setTin(20.0 + random.nextDouble() * 10); // 20-30度
                data.setTout(18.0 + random.nextDouble() * 12); // 18-30度
                data.setHin(40.0 + random.nextDouble() * 20); // 40-60%
                data.setHout(35.0 + random.nextDouble() * 25); // 35-60%
                data.setLxin(100 + random.nextInt(400)); // 100-500
                data.setBrightness(50 + random.nextInt(50)); // 50-100
                data.setVstatus(1); // 运行状态
                data.setBattery(80 + random.nextInt(20)); // 80-100%
                
                // 设置时间（最近30天内的随机时间）
                data.setCreatedAt(LocalDateTime.now().minusMinutes(random.nextInt(43200))); // 最近一个月
                
                deviceDataRepository.save(data);
            }
            
            return ApiResponse.success("成功生成 " + count + " 条测试数据");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("生成测试数据失败: " + e.getMessage());
        }
    }

    /**
     * 清空设备数据
     */
    @DeleteMapping("/clear/{vid}")
    public ApiResponse<String> clearDeviceData(@PathVariable String vid) {
        try {
            deviceDataRepository.deleteByVid(vid);
            return ApiResponse.success("已清空设备 " + vid + " 的数据");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("清空数据失败: " + e.getMessage());
        }
    }
}