package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api/test")
public class TestDataController {

    @Autowired
    private DataService dataService;

    /**
     * 为指定设备生成测试历史数据
     */
    @PostMapping("/generate-history/{vid}")
    public ApiResponse<String> generateTestData(@PathVariable String vid,
                                               @RequestParam(defaultValue = "10") int count) {
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            DeviceDataDto dto = new DeviceDataDto();
            dto.setVid(vid);
            dto.setTin(20.0 + random.nextDouble() * 10); // 温度在20-30之间
            dto.setTout(18.0 + random.nextDouble() * 10); // 外部温度在18-28之间
            dto.setHin(40.0 + random.nextDouble() * 30); // 湿度在40-70之间
            dto.setHout(35.0 + random.nextDouble() * 30); // 外部湿度在35-65之间
            dto.setLxin(1000 + random.nextInt(2000)); // 光照在1000-3000之间
            dto.setBrightness(50 + random.nextInt(50)); // 亮度在50-100之间
            dto.setTimestamp(LocalDateTime.now().minusMinutes(random.nextInt(1440))); // 随机过去一天内的数据
            
            dataService.saveDeviceData(dto);
        }
        
        return ApiResponse.success("成功为设备 " + vid + " 生成了 " + count + " 条测试数据");
    }
}