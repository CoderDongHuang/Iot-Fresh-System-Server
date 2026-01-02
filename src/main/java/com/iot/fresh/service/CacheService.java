package com.iot.fresh.service;

import com.iot.fresh.dto.DeviceDataDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheService {

    // 为设备实时数据提供缓存
    @Cacheable(value = "deviceRealTimeData", key = "#vid")
    public List<DeviceDataDto> getDeviceRealTimeDataFromCache(String vid) {
        // 这个方法只是一个缓存入口点
        // 实际的数据获取应该在DataService中完成
        return null;
    }

    @CacheEvict(value = "deviceRealTimeData", key = "#vid")
    public void evictDeviceRealTimeDataCache(String vid) {
        // 清除指定设备的实时数据缓存
    }

    @CacheEvict(value = "deviceRealTimeData", allEntries = true)
    public void evictAllDeviceRealTimeDataCache() {
        // 清除所有设备的实时数据缓存
    }
}