package com.iot.fresh.service;

import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;

import java.util.List;

/**
 * 设备服务接口
 * 提供设备管理相关功能
 * 
 * @author donghuang
 * @since 2026
 */
public interface DeviceService {
    /**
     * 获取所有设备列表
     * 
     * @return ApiResponse<List<DeviceDto>> 包含所有设备的响应对象
     */
    ApiResponse<List<DeviceDto>> getAllDevices();
    
    /**
     * 分页获取设备列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return ApiResponse<PaginatedResponse<DeviceDto>> 分页设备列表响应对象
     */
    ApiResponse<PaginatedResponse<DeviceDto>> getDevicesWithPagination(Integer pageNum, Integer pageSize);
    
    /**
     * 根据VID获取设备信息
     * 
     * @param vid 设备唯一标识符
     * @return ApiResponse<DeviceDto> 设备信息响应对象
     */
    ApiResponse<DeviceDto> getDeviceByVid(String vid);
    
    /**
     * 更新设备信息
     * 
     * @param vid 设备唯一标识符
     * @param deviceDto 设备数据传输对象
     * @return ApiResponse<DeviceDto> 更新结果响应对象
     */
    ApiResponse<DeviceDto> updateDevice(String vid, DeviceDto deviceDto);
    
    /**
     * 注册新设备
     * 
     * @param deviceDto 设备数据传输对象
     * @return ApiResponse<DeviceDto> 注册结果响应对象
     */
    ApiResponse<DeviceDto> registerDevice(DeviceDto deviceDto);
    
    /**
     * 更新设备心跳时间
     * 
     * @param vid 设备唯一标识符
     */
    void updateDeviceHeartbeat(String vid);
}