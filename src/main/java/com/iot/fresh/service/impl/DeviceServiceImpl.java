package com.iot.fresh.service.impl;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.DeviceDto;
import com.iot.fresh.entity.Device;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {
    
    private static final Logger log = LoggerFactory.getLogger(DeviceServiceImpl.class);
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Override
    public ApiResponse<String> addDevice(DeviceDto deviceDto) {
        try {
            log.info("开始新增设备 - VID: {}, 设备名称: {}", deviceDto.getVid(), deviceDto.getDeviceName());
            
            // 验证必填字段
            if (deviceDto.getVid() == null || deviceDto.getVid().trim().isEmpty()) {
                return ApiResponse.error("设备VID不能为空");
            }
            
            if (deviceDto.getDeviceName() == null || deviceDto.getDeviceName().trim().isEmpty()) {
                return ApiResponse.error("设备名称不能为空");
            }
            
            // 检查设备VID是否已存在
            if (deviceRepository.existsByVid(deviceDto.getVid())) {
                return ApiResponse.error("设备VID已存在: " + deviceDto.getVid());
            }
            
            // 创建设备实体
            Device device = new Device(
                deviceDto.getVid(),
                deviceDto.getDeviceName(),
                deviceDto.getDeviceType(),
                deviceDto.getLocation(),
                deviceDto.getStatus(),
                deviceDto.getDescription()
            );
            
            // 保存到数据库
            Device savedDevice = deviceRepository.save(device);
            
            log.info("设备新增成功 - ID: {}, VID: {}", savedDevice.getId(), savedDevice.getVid());
            
            return ApiResponse.success("设备新增成功");
            
        } catch (Exception e) {
            log.error("新增设备失败 - VID: {}, 错误: {}", deviceDto.getVid(), e.getMessage(), e);
            return ApiResponse.error("新增设备失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<DeviceDto> getDeviceByVid(String vid) {
        try {
            log.info("查询设备信息 - VID: {}", vid);
            
            if (vid == null || vid.trim().isEmpty()) {
                return ApiResponse.error("设备VID不能为空");
            }
            
            Device device = deviceRepository.findByVid(vid)
                .orElse(null);
            
            if (device == null) {
                return ApiResponse.error("设备不存在: " + vid);
            }
            
            DeviceDto deviceDto = convertToDto(device);
            return ApiResponse.success(deviceDto);
            
        } catch (Exception e) {
            log.error("查询设备信息失败 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("查询设备信息失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<List<DeviceDto>> getAllDevices() {
        try {
            log.info("查询所有设备列表");
            
            List<Device> devices = deviceRepository.findAll();
            List<DeviceDto> deviceDtos = devices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            log.info("查询到 {} 台设备", deviceDtos.size());
            return ApiResponse.success(deviceDtos);
            
        } catch (Exception e) {
            log.error("查询设备列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询设备列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<String> updateDevice(String vid, DeviceDto deviceDto) {
        try {
            log.info("更新设备信息 - VID: {}", vid);
            
            Device device = deviceRepository.findByVid(vid)
                .orElse(null);
            
            if (device == null) {
                return ApiResponse.error("设备不存在: " + vid);
            }
            
            // 更新设备信息
            if (deviceDto.getDeviceName() != null) {
                device.setDeviceName(deviceDto.getDeviceName());
            }
            if (deviceDto.getDeviceType() != null) {
                device.setDeviceType(deviceDto.getDeviceType());
            }
            if (deviceDto.getLocation() != null) {
                device.setLocation(deviceDto.getLocation());
            }
            if (deviceDto.getStatus() != null) {
                device.setStatus(deviceDto.getStatus());
            }
            if (deviceDto.getDescription() != null) {
                device.setDescription(deviceDto.getDescription());
            }
            if (deviceDto.getContactPhone() != null) {
                device.setContactPhone(deviceDto.getContactPhone());
            }
            
            deviceRepository.save(device);
            log.info("设备信息更新成功 - VID: {}", vid);
            
            return ApiResponse.success("设备信息更新成功");
            
        } catch (Exception e) {
            log.error("更新设备信息失败 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("更新设备信息失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<String> deleteDevice(String vid) {
        try {
            log.info("删除设备 - VID: {}", vid);
            
            Device device = deviceRepository.findByVid(vid)
                .orElse(null);
            
            if (device == null) {
                return ApiResponse.error("设备不存在: " + vid);
            }
            
            deviceRepository.delete(device);
            log.info("设备删除成功 - VID: {}", vid);
            
            return ApiResponse.success("设备删除成功");
            
        } catch (Exception e) {
            log.error("删除设备失败 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("删除设备失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<String> updateDeviceStatus(String vid, Integer status) {
        try {
            log.info("更新设备状态 - VID: {}, 状态: {}", vid, status);
            
            if (vid == null || vid.trim().isEmpty()) {
                return ApiResponse.error("设备VID不能为空");
            }
            
            if (status == null || (status != 0 && status != 1)) {
                return ApiResponse.error("设备状态无效: " + status);
            }
            
            Device device = deviceRepository.findByVid(vid)
                .orElse(null);
            
            if (device == null) {
                return ApiResponse.error("设备不存在: " + vid);
            }
            
            device.setStatus(status);
            device.setUpdatedAt(LocalDateTime.now());
            
            deviceRepository.save(device);
            log.info("设备状态更新成功 - VID: {}, 状态: {}", vid, status);
            
            return ApiResponse.success("设备状态更新成功");
            
        } catch (Exception e) {
            log.error("更新设备状态失败 - VID: {}, 状态: {}, 错误: {}", vid, status, e.getMessage(), e);
            return ApiResponse.error("更新设备状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<String> updateDeviceHeartbeat(String vid) {
        try {
            log.info("更新设备心跳时间 - VID: {}", vid);
            
            if (vid == null || vid.trim().isEmpty()) {
                return ApiResponse.error("设备VID不能为空");
            }
            
            Device device = deviceRepository.findByVid(vid)
                .orElse(null);
            
            if (device == null) {
                return ApiResponse.error("设备不存在: " + vid);
            }
            
            device.setLastHeartbeat(LocalDateTime.now());
            device.setUpdatedAt(LocalDateTime.now());
            
            deviceRepository.save(device);
            log.info("设备心跳时间更新成功 - VID: {}", vid);
            
            return ApiResponse.success("设备心跳时间更新成功");
            
        } catch (Exception e) {
            log.error("更新设备心跳时间失败 - VID: {}, 错误: {}", vid, e.getMessage(), e);
            return ApiResponse.error("更新设备心跳时间失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Device实体转换为DeviceDto
     */
    private DeviceDto convertToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setVid(device.getVid());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setStatus(device.getStatus());
        dto.setLocation(device.getLocation());
        dto.setContactPhone(device.getContactPhone());
        dto.setDescription(device.getDescription());
        dto.setManufacturer(device.getManufacturer());
        dto.setModel(device.getModel());
        dto.setFirmwareVersion(device.getFirmwareVersion());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        dto.setLastHeartbeat(device.getLastHeartbeat());
        return dto;
    }
}