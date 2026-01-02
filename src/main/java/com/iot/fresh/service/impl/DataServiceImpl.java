package com.iot.fresh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.repository.DeviceDataRepository;
import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private DeviceDataRepository deviceDataRepository;

    @Override
    public ApiResponse<DeviceDataDto> saveDeviceData(DeviceDataDto deviceDataDto) {
        DeviceData deviceData = new DeviceData();
        deviceData.setVid(deviceDataDto.getVid());
        deviceData.setDeviceType(deviceDataDto.getDeviceType());
        deviceData.setTin(deviceDataDto.getTin());
        deviceData.setTout(deviceDataDto.getTout());
        deviceData.setLxin(deviceDataDto.getLxin());
        deviceData.setPid(deviceDataDto.getPid());
        deviceData.setVstatus(deviceDataDto.getVstatus());
        deviceData.setBattery(deviceDataDto.getBattery());
        deviceData.setBrightness(deviceDataDto.getBrightness());
        deviceData.setSpeedM1(deviceDataDto.getSpeedM1());
        deviceData.setSpeedM2(deviceDataDto.getSpeedM2());

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
        // 解析MQTT消息并保存到数据库
        try {
            // 这里应该使用JSON解析库，如Jackson或Gson
            // 为简化，这里使用简单的字符串解析
            DeviceDataDto deviceDataDto = parseDeviceDataFromJson(vid, payload);
            if (deviceDataDto != null) {
                saveDeviceData(deviceDataDto);
                System.out.println("Received and saved device data for VID: " + vid);
            }
        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DeviceDataDto parseDeviceDataFromJson(String vid, String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
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
            
            if (json.contains("\"lxin\"")) {
                int start = json.indexOf("\"lxin\":") + 7;
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
        dto.setDeviceType(deviceData.getDeviceType());
        dto.setTin(deviceData.getTin());
        dto.setTout(deviceData.getTout());
        dto.setLxin(deviceData.getLxin());
        dto.setPid(deviceData.getPid());
        dto.setVstatus(deviceData.getVstatus());
        dto.setBattery(deviceData.getBattery());
        dto.setBrightness(deviceData.getBrightness());
        dto.setSpeedM1(deviceData.getSpeedM1());
        dto.setSpeedM2(deviceData.getSpeedM2());
        dto.setTimestamp(deviceData.getCreatedAt());
        return dto;
    }
}