package com.iot.fresh.service.impl;

import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;
import com.iot.fresh.entity.Device;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    @Cacheable(value = "devices", key = "'all'")
    public ApiResponse<List<DeviceDto>> getAllDevices() {
        List<Device> devices = deviceRepository.findAll();
        List<DeviceDto> deviceDtos = devices.stream().map(this::convertToDeviceDto).collect(Collectors.toList());
        return ApiResponse.success(deviceDtos);
    }

    @Override
    @Cacheable(value = "devices", key = "#vid")
    public ApiResponse<DeviceDto> getDeviceByVid(String vid) {
        Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
        if (deviceOpt.isPresent()) {
            DeviceDto deviceDto = convertToDeviceDto(deviceOpt.get());
            return ApiResponse.success(deviceDto);
        }
        return ApiResponse.error("设备不存在");
    }

    @Override
    @CacheEvict(value = {"devices"}, allEntries = true) // 清除所有设备缓存，包括all和具体设备
    public ApiResponse<DeviceDto> updateDevice(String vid, DeviceDto deviceDto) {
        Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setDeviceName(deviceDto.getDeviceName());
            device.setDeviceType(deviceDto.getDeviceType());
            device.setStatus(deviceDto.getStatus());
            device.setLocation(deviceDto.getLocation());
            device.setContactPhone(deviceDto.getContactPhone());
            device.setDescription(deviceDto.getDescription());
            
            Device updatedDevice = deviceRepository.save(device);
            DeviceDto updatedDeviceDto = convertToDeviceDto(updatedDevice);
            return ApiResponse.success("设备更新成功", updatedDeviceDto);
        }
        return ApiResponse.error("设备不存在");
    }

    @Override
    @CacheEvict(value = {"devices"}, allEntries = true) // 清除所有设备缓存
    public ApiResponse<DeviceDto> registerDevice(DeviceDto deviceDto) {
        Device device = new Device();
        device.setVid(deviceDto.getVid());
        device.setDeviceName(deviceDto.getDeviceName());
        device.setDeviceType(deviceDto.getDeviceType());
        device.setStatus(deviceDto.getStatus());
        device.setLocation(deviceDto.getLocation());
        device.setContactPhone(deviceDto.getContactPhone());
        device.setDescription(deviceDto.getDescription());
        
        Device savedDevice = deviceRepository.save(device);
        DeviceDto savedDeviceDto = convertToDeviceDto(savedDevice);
        return ApiResponse.success("设备注册成功", savedDeviceDto);
    }

    @Override
    public ApiResponse<PaginatedResponse<DeviceDto>> getDevicesWithPagination(Integer pageNum, Integer pageSize) {
        try {
            // 确保页码和页面大小为正数
            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 10;
            
            System.out.println("Debug - pageNum: " + pageNum + ", pageSize: " + pageSize);
            
            // 使用JPA的分页功能
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
            
            org.springframework.data.domain.Page<Device> devicePage = deviceRepository.findAll(pageable);
            List<DeviceDto> deviceDtos = devicePage.getContent().stream()
                    .map(this::convertToDeviceDto)
                    .collect(Collectors.toList());
            
            System.out.println("Debug - Found " + deviceDtos.size() + " devices");
            
            // 创建分页响应对象，符合前端期望的格式
            PaginatedResponse<DeviceDto> paginatedResponse = new PaginatedResponse<>(
                deviceDtos, 
                devicePage.getTotalElements(), 
                pageNum, 
                pageSize
            );
            
            return ApiResponse.success(paginatedResponse);
        } catch (Exception e) {
            System.err.println("Error in getDevicesWithPagination: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("获取设备列表失败: " + e.getMessage());
        }
    }

    @Override
    public void updateDeviceHeartbeat(String vid) {
        Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setLastHeartbeat(LocalDateTime.now());
            deviceRepository.save(device);
        }
    }

    private DeviceDto convertToDeviceDto(Device device) {
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setId(device.getId());
        deviceDto.setVid(device.getVid());
        deviceDto.setDeviceName(device.getDeviceName());
        deviceDto.setDeviceType(device.getDeviceType());
        deviceDto.setStatus(device.getStatus());
        deviceDto.setLocation(device.getLocation());
        deviceDto.setContactPhone(device.getContactPhone());
        deviceDto.setDescription(device.getDescription());
        
        // 设置多种时间格式以满足API规范
        deviceDto.setLastOnlineTime(device.getLastHeartbeat());
        deviceDto.setLastOnline_time(device.getLastHeartbeat());
        deviceDto.setLast_heartbeat(device.getLastHeartbeat());
        deviceDto.setLastHeartbeat(device.getLastHeartbeat());
        deviceDto.setCreateTime(device.getCreatedAt());
        deviceDto.setCreate_time(device.getCreatedAt());
        deviceDto.setUpdatedAt(device.getUpdatedAt());
        
        return deviceDto;
    }
}