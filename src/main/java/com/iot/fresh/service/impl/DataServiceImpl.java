package com.iot.fresh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.Device;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private DeviceDataRepository deviceDataRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ApiResponse<DeviceDataDto> saveDeviceData(DeviceDataDto deviceDataDto) {
        DeviceData deviceData = new DeviceData();
        deviceData.setVid(deviceDataDto.getVid());
        deviceData.setDeviceName(deviceDataDto.getDeviceName());
        deviceData.setDeviceType(deviceDataDto.getDeviceType());
        deviceData.setTin(deviceDataDto.getTin());
        deviceData.setTout(deviceDataDto.getTout());
        deviceData.setHin(deviceDataDto.getHin());
        deviceData.setHout(deviceDataDto.getHout());
        deviceData.setLxin(deviceDataDto.getLxin());
        deviceData.setLight(deviceDataDto.getLight());
        deviceData.setPid(deviceDataDto.getPid());
        deviceData.setVstatus(deviceDataDto.getVstatus());
        deviceData.setBattery(deviceDataDto.getBattery());
        deviceData.setBrightness(deviceDataDto.getBrightness());
        deviceData.setSpeedM1(deviceDataDto.getSpeedM1());
        deviceData.setSpeedM2(deviceDataDto.getSpeedM2());
        deviceData.setTimestamp(deviceDataDto.getTimestamp()); // 使用转换后的时间戳

        DeviceData savedData = deviceDataRepository.save(deviceData);
        DeviceDataDto savedDto = convertToDeviceDataDto(savedData);
        return ApiResponse.success("数据保存成功", savedDto);
    }

    @Override
    public ApiResponse<List<DeviceDataDto>> getDeviceRealTimeData(String vid) {
        List<DeviceData> deviceDataList = deviceDataRepository.findByVid(vid);
        List<DeviceDataDto> deviceDataDtos = deviceDataList.stream()
                .map(this::convertToDeviceDataDto)
                .collect(Collectors.toList());
        return ApiResponse.success(deviceDataDtos);
    }

    @Override
    public ApiResponse<List<DeviceDataDto>> getDeviceHistoryData(String vid, LocalDateTime startTime, LocalDateTime endTime) {
        List<DeviceData> deviceDataList = deviceDataRepository.findByVidAndTimeRange(vid, startTime, endTime);
        List<DeviceDataDto> deviceDataDtos = deviceDataList.stream()
                .map(this::convertToDeviceDataDto)
                .collect(Collectors.toList());
        return ApiResponse.success(deviceDataDtos);
    }

    @Override
    public void processDeviceDataFromMqtt(String vid, String payload) {
        // 解析MQTT消息并根据内容决定处理方式
        try {
            DeviceDataDto deviceDataDto = parseDeviceDataFromJson(vid, payload);
            if (deviceDataDto != null) {
                // 输出vstatus的值以进行调试
                System.out.println("Received device data for VID: " + vid + ", vstatus: " + deviceDataDto.getVstatus());
                
                // 检查是否是设备状态更新（包含vstatus字段）
                if (deviceDataDto.getVstatus() != null) {
                    // 这是设备状态更新，更新设备主状态
                    System.out.println("Received status update, updating device status for VID: " + vid + ", vstatus: " + deviceDataDto.getVstatus());
                    updateDeviceMainStatus(vid, deviceDataDto.getVstatus());
                    
                    // 对于状态更新，不存储到device_data表
                    System.out.println("Status update processed for VID: " + vid + ", skipped saving to device_data table");
                } else {
                    // 这是普通数据上报，包含温度、湿度、速度、亮度等参数，存储到device_data表
                    saveDeviceData(deviceDataDto);
                    System.out.println("Received and saved device data for VID: " + vid + 
                        ", tin: " + deviceDataDto.getTin() + 
                        ", tout: " + deviceDataDto.getTout() + 
                        ", hin: " + deviceDataDto.getHin() + 
                        ", hout: " + deviceDataDto.getHout() + 
                        ", lxin: " + deviceDataDto.getLxin() + 
                        ", light: " + deviceDataDto.getLight() + 
                        ", battery: " + deviceDataDto.getBattery() + 
                        ", brightness: " + deviceDataDto.getBrightness() + 
                        ", speedM1: " + deviceDataDto.getSpeedM1() + 
                        ", speedM2: " + deviceDataDto.getSpeedM2());
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 更新设备的主状态（在线/离线/故障等）
     */
    private void updateDeviceMainStatus(String vid, Integer vstatus) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                
                // 将vstatus转换为设备状态
                // 假设vstatus: 0=离线, 1=在线, 2=故障, 3=维护
                // 这里可以根据实际需要调整映射关系
                device.setStatus(vstatus);
                device.setLastHeartbeat(LocalDateTime.now());
                
                deviceRepository.save(device);
                System.out.println("Updated device status for VID: " + vid + ", status: " + vstatus);
            }
        } catch (Exception e) {
            System.err.println("Error updating device status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DeviceDataDto parseDeviceDataFromJson(String vid, String json) {
        try {
            DeviceDataDto dto = objectMapper.readValue(json, DeviceDataDto.class);
            // 如果JSON中没有VID，使用传入的VID
            if (dto.getVid() == null || dto.getVid().isEmpty()) {
                dto.setVid(vid);
            }
            return dto;
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            // 如果Jackson解析失败，回退到手动解析
            return parseDeviceDataManually(vid, json);
        }
    }
    
    // 手动解析方法作为备用
    private DeviceDataDto parseDeviceDataManually(String vid, String json) {
        try {
            DeviceDataDto dto = new DeviceDataDto();
            dto.setVid(vid); // 使用传入的VID
            
            // 简单解析JSON字符串
            if (json.contains("\"deviceType\"")) {
                int start = json.indexOf("\"deviceType\":\"") + 14;
                int end = json.indexOf("\"", start);
                if (start > 13 && end > start) {
                    dto.setDeviceType(json.substring(start, end));
                }
            }
            
            if (json.contains("\"tin\"")) {
                int start = json.indexOf("\"tin\":") + 6;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 5 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTin(Double.parseDouble(value));
                }
            }
            
            if (json.contains("\"tout\"")) {
                int start = json.indexOf("\"tout\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTout(Double.parseDouble(value));
                }
            }
            
            // 支持小写"lxin"格式
            if (json.contains("\"lxin\"")) {
                int start = json.indexOf("\"lxin\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setLxin(Integer.parseInt(value));
                }
            }
            // 支持大写"LXin"格式（来自硬件）
            else if (json.contains("\"LXin\"")) {
                int start = json.indexOf("\"LXin\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setLxin(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"pid\"")) {
                int start = json.indexOf("\"pid\":\"") + 7;
                int end = json.indexOf("\"", start);
                if (start > 6 && end > start) {
                    dto.setPid(json.substring(start, end));
                }
            }
            
            if (json.contains("\"vstatus\"")) {
                int start = json.indexOf("\"vstatus\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setVstatus(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"battery\"")) {
                int start = json.indexOf("\"battery\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setBattery(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"brightness\"")) {
                int start = json.indexOf("\"brightness\":") + 13;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 12 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setBrightness(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"speedM1\"")) {
                int start = json.indexOf("\"speedM1\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setSpeedM1(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"speedM2\"")) {
                int start = json.indexOf("\"speedM2\":") + 10;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 9 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setSpeedM2(Integer.parseInt(value));
                }
            }
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error parsing JSON manually: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private DeviceDataDto convertToDeviceDataDto(DeviceData deviceData) {
        DeviceDataDto dto = new DeviceDataDto();
        dto.setId(deviceData.getId());
        dto.setVid(deviceData.getVid());
        dto.setDeviceName(deviceData.getDeviceName());
        dto.setDeviceType(deviceData.getDeviceType());
        dto.setTin(deviceData.getTin());
        dto.setTout(deviceData.getTout());
        dto.setHin(deviceData.getHin());
        dto.setHout(deviceData.getHout());
        dto.setLxin(deviceData.getLxin());
        dto.setLight(deviceData.getLight());
        dto.setPid(deviceData.getPid());
        dto.setVstatus(deviceData.getVstatus());
        dto.setBattery(deviceData.getBattery());
        dto.setBrightness(deviceData.getBrightness());
        dto.setSpeedM1(deviceData.getSpeedM1());
        dto.setSpeedM2(deviceData.getSpeedM2());
        // 设置转换后的时间戳，使用设备数据的时间戳或创建时间
        if (deviceData.getTimestamp() != null) {
            dto.setTimestamp(deviceData.getTimestamp());
        } else {
            dto.setTimestamp(deviceData.getCreatedAt());
        }
        return dto;
    }
    
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
        List<Map<String, Object>> result = deviceDataList.stream().map(data -> {
            Map<String, Object> item = new java.util.HashMap<>();
            // 使用ISO 8601格式的时间字符串
            item.put("timestamp", data.getCreatedAt().toString());
            // 优先使用lxin字段，如果没有则使用light字段
            Integer lightValue = data.getLxin() != null ? data.getLxin() : data.getLight();
            item.put("value", lightValue != null ? lightValue : 0);
            return item;
        }).collect(java.util.stream.Collectors.toList());

        return ApiResponse.success("获取成功", result);
    }
    
    @Override
    public void updateDeviceStatus(String vid, Integer status) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                device.setStatus(status);
                device.setLastHeartbeat(LocalDateTime.now());
                deviceRepository.save(device);
                System.out.println("Updated device status in devices table for VID: " + vid + ", status: " + status);
            }
        } catch (Exception e) {
            System.err.println("Error updating device status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public DeviceData getLatestDeviceData(String vid) {
        return deviceDataRepository.findTopByVidOrderByCreatedAtDesc(vid);
    }
}