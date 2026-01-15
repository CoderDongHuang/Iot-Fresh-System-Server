package com.iot.fresh.service;

import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.DeviceData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据服务接口
 * 提供设备数据管理相关功能
 * 
 * @author donghuang
 * @since 2026
 */
public interface DataService {
    /**
     * 保存设备数据
     * 
     * @param deviceDataDto 设备数据传输对象
     * @return ApiResponse<DeviceDataDto> 保存结果响应对象
     */
    ApiResponse<DeviceDataDto> saveDeviceData(DeviceDataDto deviceDataDto);
    
    /**
     * 获取设备实时数据
     * 
     * @param vid 设备唯一标识符
     * @return ApiResponse<List<DeviceDataDto>> 实时数据响应对象
     */
    ApiResponse<List<DeviceDataDto>> getDeviceRealTimeData(String vid);
    
    /**
     * 获取设备历史数据
     * 
     * @param vid 设备唯一标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return ApiResponse<List<DeviceDataDto>> 历史数据响应对象
     */
    ApiResponse<List<DeviceDataDto>> getDeviceHistoryData(String vid, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取光照数据
     * 
     * @param vid 设备唯一标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return ApiResponse<List<Map<String, Object>>> 光照数据响应对象
     */
    ApiResponse<List<Map<String, Object>>> getLightDataByVid(String vid, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 处理由MQTT接收的设备数据
     * 
     * @param vid 设备唯一标识符
     * @param payload MQTT消息负载
     */
    void processDeviceDataFromMqtt(String vid, String payload);
    
    /**
     * 更新设备状态
     * 
     * @param vid 设备唯一标识符
     * @param status 设备状态
     */
    void updateDeviceStatus(String vid, Integer status);
    
    /**
     * 获取设备最新数据
     * 
     * @param vid 设备唯一标识符
     * @return DeviceData 最新设备数据实体
     */
    DeviceData getLatestDeviceData(String vid);
}