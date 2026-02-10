package com.iot.fresh.service;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import java.util.List;

public interface DeviceService {
    
    /**
     * 新增设备
     * @param deviceDto 设备信息
     * @return 操作结果
     */
    ApiResponse<String> addDevice(DeviceDto deviceDto);
    
    /**
     * 根据VID获取设备信息
     * @param vid 设备VID
     * @return 设备信息
     */
    ApiResponse<DeviceDto> getDeviceByVid(String vid);
    
    /**
     * 获取所有设备列表
     * @return 设备列表
     */
    ApiResponse<List<DeviceDto>> getAllDevices();
    
    /**
     * 更新设备信息
     * @param vid 设备VID
     * @param deviceDto 设备信息
     * @return 操作结果
     */
    ApiResponse<String> updateDevice(String vid, DeviceDto deviceDto);
    
    /**
     * 删除设备
     * @param vid 设备VID
     * @return 操作结果
     */
    ApiResponse<String> deleteDevice(String vid);
    
    /**
     * 更新设备状态
     * @param vid 设备VID
     * @param status 状态值
     * @return 操作结果
     */
    ApiResponse<String> updateDeviceStatus(String vid, Integer status);
    
    /**
     * 更新设备心跳时间
     * @param vid 设备VID
     * @return 操作结果
     */
    ApiResponse<String> updateDeviceHeartbeat(String vid);
}