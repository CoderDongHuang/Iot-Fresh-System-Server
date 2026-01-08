package com.iot.fresh.service.impl;

import com.iot.fresh.dto.*;
import com.iot.fresh.entity.Device;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.DeviceManagementService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeviceManagementServiceImpl implements DeviceManagementService {

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private DeviceDataRepository deviceDataRepository;

    @Override
    public ApiResponse<PaginatedResponse<DeviceDto>> getDeviceList(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        // 创建分页请求
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        
        Page<Device> devicePage;
        if (keyword != null && !keyword.isEmpty()) {
            if (status != null) {
                // 在设备名称或VID中搜索，并按状态过滤
                devicePage = deviceRepository.findByDeviceNameContainingOrVidContainingAndStatus(keyword, status, pageable);
            } else {
                // 在设备名称或VID中搜索，不按状态过滤
                devicePage = deviceRepository.findByDeviceNameContainingOrVidContaining(keyword, pageable);
            }
        } else {
            if (status != null) {
                devicePage = deviceRepository.findByStatus(status, pageable);
            } else {
                devicePage = deviceRepository.findAll(pageable);
            }
        }
        
        // 转换为DeviceDto列表
        List<DeviceDto> deviceDtos = devicePage.getContent().stream().map(this::convertToDeviceDto).toList();
        
        // 创建分页响应
        PaginatedResponse<DeviceDto> paginatedResponse = new PaginatedResponse<>(
                deviceDtos,
                devicePage.getTotalElements(),
                pageNum,
                pageSize
        );
        
        return ApiResponse.success(paginatedResponse);
    }

    @Override
    public ApiResponse<DeviceDetailDto> getDeviceDetail(String vid) {
        Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
        if (!deviceOpt.isPresent()) {
            return ApiResponse.error("设备不存在");
        }
        
        Device device = deviceOpt.get();
        DeviceDetailDto detailDto = new DeviceDetailDto();
        
        // 复制基本属性
        BeanUtils.copyProperties(device, detailDto);
        detailDto.setVid(device.getVid());
        detailDto.setDeviceName(device.getDeviceName());
        detailDto.setDeviceType(device.getDeviceType());
        detailDto.setStatus(device.getStatus());
        detailDto.setLocation(device.getLocation());
        detailDto.setContactPhone(device.getContactPhone());
        detailDto.setDescription(device.getDescription());
        detailDto.setCreateTime(device.getCreatedAt());
        detailDto.setLastOnlineTime(device.getLastHeartbeat());
        detailDto.setRemarks(device.getDescription()); // 使用描述字段作为备注
        
        // 获取设备当前数据
        DeviceCurrentDataDto currentData = getCurrentDataForDevice(vid);
        detailDto.setCurrentData(currentData);
        
        return ApiResponse.success(detailDto);
    }

    @Override
    public ApiResponse<DeviceCurrentDataDto> getRealTimeData(String vid) {
        DeviceCurrentDataDto currentData = getCurrentDataForDevice(vid);
        if (currentData != null) {
            return ApiResponse.success(currentData);
        } else {
            return ApiResponse.error("未找到设备实时数据");
        }
    }

    @Override
    public ApiResponse<DeviceStatusStatsDto> getStatusStats() {
        DeviceStatusStatsDto stats = new DeviceStatusStatsDto();
        
        // 总设备数
        long totalDevices = deviceRepository.count();
        stats.setTotalDevices((int) totalDevices);
        
        // 在线设备数 (状态为1)
        long onlineDevices = deviceRepository.countByStatus(1);
        stats.setOnlineDevices((int) onlineDevices);
        
        // 离线设备数 (状态为0)
        long offlineDevices = deviceRepository.countByStatus(0);
        stats.setOfflineDevices((int) offlineDevices);
        
        // 故障设备数 (状态为2)
        long faultDevices = deviceRepository.countByStatus(2);
        stats.setFaultDevices((int) faultDevices);
        
        return ApiResponse.success(stats);
    }

    @Override
    public ApiResponse<String> controlDevice(String vid, Map<String, Object> controlCommand) {
        // 验证设备是否存在
        Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
        if (!deviceOpt.isPresent()) {
            return ApiResponse.error("设备不存在");
        }
        
        Device device = deviceOpt.get();
        
        // 验证设备状态，只有在线设备才能接收控制指令
        if (device.getStatus() != 1) { // 1 表示在线
            return ApiResponse.error(400, "设备当前离线，无法执行控制命令");
        }
        
        // 获取命令类型
        String command = (String) controlCommand.get("command");
        if (command == null || command.trim().isEmpty()) {
            return ApiResponse.error(400, "命令类型不能为空");
        }
        
        // 验证命令是否被支持
        boolean isSupportedCommand = isValidCommand(command);
        if (!isSupportedCommand) {
            return ApiResponse.error(400, "不支持的控制命令");
        }
        
        // 执行命令（通过MQTT发送到设备）
        try {
            // 这里应该通过MQTT发送控制指令到设备
            System.out.println("发送控制指令到设备 " + vid + ": " + controlCommand);
            
            // 发送MQTT消息
            String topic = "device/" + vid + "/control";
            // 将控制命令转换为适当的格式并发送
            String commandMessage = buildCommandMessage(command, controlCommand.get("params"));
            
            // TODO: 实际的MQTT发布逻辑应该在这里
            // mqttPublisher.publish(topic, commandMessage);
            
            return ApiResponse.success("控制指令发送成功");
        } catch (Exception e) {
            System.err.println("发送控制指令失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(500, "控制命令执行失败");
        }
    }
    
    private boolean isValidCommand(String command) {
        switch (command.toLowerCase()) {
            case "turnon":
            case "turnoff":
            case "restart":
            case "reset":
            case "settemp":
            case "setbrightness":
            case "setspeed":
            case "calibrate":
            case "selfcheck":
            case "updatefirmware":
                return true;
            default:
                return false;
        }
    }
    
    private String buildCommandMessage(String command, Object params) {
        // 构建发送到设备的命令消息
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"command\":\"").append(command).append("\"");
        
        if (params instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paramMap = (Map<String, Object>) params;
            if (!paramMap.isEmpty()) {
                sb.append(",\"params\":{");
                boolean first = true;
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append("\"").append(entry.getKey()).append("\":");
                    if (entry.getValue() instanceof String) {
                        sb.append("\"").append(entry.getValue()).append("\"");
                    } else {
                        sb.append(entry.getValue());
                    }
                    first = false;
                }
                sb.append("}");
            }
        }
        sb.append("}");
        
        return sb.toString();
    }
    
    private DeviceDto convertToDeviceDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setVid(device.getVid());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setStatus(device.getStatus());
        dto.setLocation(device.getLocation());
        dto.setLastOnlineTime(device.getLastHeartbeat());
        dto.setCreatedAt(device.getCreatedAt());
        
        // 获取设备当前数据
        DeviceCurrentDataDto currentData = getCurrentDataForDevice(device.getVid());
        dto.setCurrentData(currentData);
        
        return dto;
    }
    
    private DeviceCurrentDataDto getCurrentDataForDevice(String vid) {
        // 获取设备最新的数据
        List<DeviceData> deviceDataList = deviceDataRepository.findByVidOrderByCreatedAtDesc(vid, PageRequest.of(0, 1));
        
        if (!deviceDataList.isEmpty()) {
            DeviceData latestData = deviceDataList.get(0);
            DeviceCurrentDataDto currentData = new DeviceCurrentDataDto();
            currentData.setTin(latestData.getTin());
            currentData.setTout(latestData.getTout());
            currentData.setLxin(latestData.getLxin());
            currentData.setVStatus(latestData.getVstatus());
            currentData.setBattery(latestData.getBattery());
            currentData.setBrightness(latestData.getBrightness());
            currentData.setSpeedM1(latestData.getSpeedM1());
            currentData.setSpeedM2(latestData.getSpeedM2());
            currentData.setHin(latestData.getHin());
            currentData.setHout(latestData.getHout());
            
            return currentData;
        }
        
        // 如果没有数据，返回空对象
        return new DeviceCurrentDataDto();
    }
}