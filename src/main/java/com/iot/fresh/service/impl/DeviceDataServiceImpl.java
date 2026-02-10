package com.iot.fresh.service.impl;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import com.iot.fresh.service.DeviceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceDataServiceImpl implements DeviceDataService {

    @Autowired
    private DeviceDataRepository deviceDataRepository;

    @Override
    public ApiResponse<List<Map<String, Object>>> getLightDataByVid(String vid, LocalDateTime startTime, LocalDateTime endTime) {
        // 设置默认时间范围（1小时前到现在）
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        // 查询设备光照数据
        List<DeviceData> deviceDataList = deviceDataRepository.findByVidAndTimeRange(vid, startTime, endTime);

        // 转换为前端期望的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceData data : deviceDataList) {
            Map<String, Object> item = new HashMap<>();
            // 使用ISO 8601格式的时间字符串
            item.put("time", data.getCreatedAt().toString());
            // 使用lxin字段作为光照数据
            Integer lightValue = data.getLxin() != null ? data.getLxin() : 0;
            item.put("value", lightValue);
            result.add(item);
        }

        return ApiResponse.success("获取成功", result);
    }
}