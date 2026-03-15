package com.iot.fresh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.Device;
import com.iot.fresh.entity.DeviceData;
import com.iot.fresh.entity.DeviceDataHistory;
import com.iot.fresh.repository.DeviceDataRepository;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.DataService;
import com.iot.fresh.service.DeviceDataHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    
    @Autowired
    private DeviceDataHistoryService deviceDataHistoryService;

    /**
     * 从设备表获取对应设备的状态作为默认值
     */
    private Integer getDeviceStatusFromDeviceTable(String vid) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                Integer status = device.getStatus();
                System.out.println("Found device in devices table - VID: " + vid + ", status: " + status);
                return status;
            } else {
                System.out.println("Device not found in devices table for VID: " + vid + ", using default status: 1");
                return 1; // 默认在线状态
            }
        } catch (Exception e) {
            System.err.println("Error getting device status from device table: " + e.getMessage());
            return 1; // 默认在线状态
        }
    }

    @Override
    public ApiResponse<DeviceDataDto> saveDeviceData(DeviceDataDto deviceDataDto) {
        System.out.println("=== Starting device data save ===");
        System.out.println("DeviceDataDto timestamp: " + deviceDataDto.getTimestamp());
        System.out.println("DeviceDataDto vstatus: " + deviceDataDto.getVstatus());
        
        // 检查是否是纯状态更新（只有vstatus字段有值，其他数据字段为null）
        boolean isStatusOnlyUpdate = isStatusOnlyUpdate(deviceDataDto);
        
        if (isStatusOnlyUpdate) {
            System.out.println("Detected status-only update");
            // 纯状态更新，查找最新记录并只更新状态字段
            DeviceData latestData = deviceDataRepository.findTopByVidOrderByCreatedAtDesc(deviceDataDto.getVid());
            
            if (latestData != null) {
                // 只更新状态字段和更新时间
                System.out.println("Status-only update, updating vstatus and updated_at fields");
                latestData.setVstatus(deviceDataDto.getVstatus());
                latestData.setUpdatedAt(LocalDateTime.now()); // 后续提交：更新 updated_at 为当前时间
                DeviceData savedData = deviceDataRepository.save(latestData);
                
                System.out.println("Updated vstatus to: " + savedData.getVstatus());
                System.out.println("Updated updated_at to: " + savedData.getUpdatedAt());
                System.out.println("Other data fields remain unchanged");
            } else {
                // 没有记录，创建新记录
                System.out.println("No existing record, creating new record for status update");
                DeviceData deviceData = new DeviceData();
                deviceData.setVid(deviceDataDto.getVid());
                
                // 参考设备表对应设备的状态作为默认值
                Integer deviceStatus = getDeviceStatusFromDeviceTable(deviceDataDto.getVid());
                deviceData.setVstatus(deviceStatus);
                deviceData.setTimestamp(LocalDateTime.now()); // 使用当前时间作为时间戳
                
                // 首次提交：设置 updated_at 为当前时间
                deviceData.setUpdatedAt(LocalDateTime.now());
                
                DeviceData savedData = deviceDataRepository.save(deviceData);
                System.out.println("Created new record with vstatus: " + savedData.getVstatus() + " (default from device table)");
                System.out.println("First submission: updated_at set to " + savedData.getUpdatedAt());
            }
        } else {
            // 普通数据更新，使用原有逻辑
            System.out.println("Regular data update");
            
            // 检查是否有相同数据的记录
            DeviceData latestData = deviceDataRepository.findTopByVidOrderByCreatedAtDesc(deviceDataDto.getVid());
            
            if (latestData != null && isDataUnchanged(latestData, deviceDataDto)) {
                // 数据没有变化，更新 updated_at 为当前时间
                System.out.println("Data unchanged, updating updated_at field");
                
                // 保存原始的时间戳和状态值，避免被覆盖
                LocalDateTime originalTimestamp = latestData.getTimestamp();
                Integer originalVstatus = latestData.getVstatus();
                LocalDateTime originalCreatedAt = latestData.getCreatedAt();
                
                // 后续提交：更新 updated_at 为当前时间
                latestData.setUpdatedAt(LocalDateTime.now());
                DeviceData savedData = deviceDataRepository.save(latestData);
                
                // 恢复原始的时间戳和状态值
                savedData.setTimestamp(originalTimestamp);
                savedData.setVstatus(originalVstatus);
                
                System.out.println("Updated updated_at: " + savedData.getUpdatedAt());
                System.out.println("Preserved timestamp: " + savedData.getTimestamp());
                System.out.println("Preserved vstatus: " + savedData.getVstatus());
                System.out.println("Preserved created_at: " + savedData.getCreatedAt());
                System.out.println("Data fields remain unchanged");
                
                // 验证后续提交时 created_at 保持不变，updated_at 更新
                if (originalCreatedAt != null && savedData.getCreatedAt() != null && 
                    originalCreatedAt.equals(savedData.getCreatedAt())) {
                    System.out.println("✓ Subsequent submission: created_at remains unchanged");
                }
                if (savedData.getUpdatedAt() != null && originalCreatedAt != null && 
                    !savedData.getUpdatedAt().equals(originalCreatedAt)) {
                    System.out.println("✓ Subsequent submission: updated_at has been updated");
                }
            } else {
                // 数据有变化或没有记录，更新现有记录或创建新记录
                if (latestData != null) {
                    // 数据有变化，更新现有记录
                    System.out.println("Data changed, updating existing record");
                    
                    // 详细记录当前数据和传入数据的差异
                    System.out.println("Current data - Tin: " + latestData.getTin() + ", Tout: " + latestData.getTout());
                    System.out.println("New data - Tin: " + deviceDataDto.getTin() + ", Tout: " + deviceDataDto.getTout());
                    
                    // 更新数据字段
                    latestData.setTin(deviceDataDto.getTin());
                    latestData.setTout(deviceDataDto.getTout());
                    latestData.setHin(deviceDataDto.getHin());
                    latestData.setHout(deviceDataDto.getHout());
                    latestData.setLxin(deviceDataDto.getLxin());
                    latestData.setLxout(deviceDataDto.getLxout());
                    latestData.setBrightness(deviceDataDto.getBrightness());
                    latestData.setTimestamp(deviceDataDto.getTimestamp()); // 数据时间戳 = 新的时间戳
                    
                    // 设备数据表的状态应该始终和设备表保持一致
                    Integer deviceStatus = getDeviceStatusFromDeviceTable(deviceDataDto.getVid());
                    latestData.setVstatus(deviceStatus);
                    System.out.println("Updated vstatus to match device table: " + deviceStatus);
                    
                    System.out.println("DeviceData before save - Tin: " + latestData.getTin() + ", timestamp: " + latestData.getTimestamp());
                    
                    // 后续提交：更新 updated_at 为当前时间
                    latestData.setUpdatedAt(LocalDateTime.now());
                    DeviceData savedData = deviceDataRepository.save(latestData);
                    
                    System.out.println("DeviceData after save - Tin: " + savedData.getTin() + ", timestamp: " + savedData.getTimestamp());
                    System.out.println("Updated data fields");
                    System.out.println("New timestamp: " + savedData.getTimestamp());
                    System.out.println("Database updated_at: " + savedData.getUpdatedAt());
                } else {
                    // 没有现有记录，创建新记录
                    System.out.println("No existing record, creating new record");
                    DeviceData deviceData = new DeviceData();
                    deviceData.setVid(deviceDataDto.getVid());
                    deviceData.setTin(deviceDataDto.getTin());
                    deviceData.setTout(deviceDataDto.getTout());
                    deviceData.setHin(deviceDataDto.getHin());
                    deviceData.setHout(deviceDataDto.getHout());
                    deviceData.setLxin(deviceDataDto.getLxin());
                    deviceData.setLxout(deviceDataDto.getLxout());
                    // 设备数据表的状态应该始终和设备表保持一致
                    Integer deviceStatus = getDeviceStatusFromDeviceTable(deviceDataDto.getVid());
                    deviceData.setVstatus(deviceStatus);
                    deviceData.setBrightness(deviceDataDto.getBrightness());
                    deviceData.setTimestamp(deviceDataDto.getTimestamp()); // 数据时间戳 = 设备提供的时间戳
                    
                    // 首次提交：设置 updated_at 为当前时间
                    deviceData.setUpdatedAt(LocalDateTime.now());
                    System.out.println("DeviceData timestamp before save: " + deviceData.getTimestamp());
                    System.out.println("DeviceData vstatus before save: " + deviceData.getVstatus());
                    System.out.println("First submission: updated_at set to " + deviceData.getUpdatedAt());

                    DeviceData savedData = deviceDataRepository.save(deviceData);
                    System.out.println("DeviceData timestamp after save: " + savedData.getTimestamp());
                    System.out.println("DeviceData vstatus after save: " + savedData.getVstatus());
                    System.out.println("DeviceData created_at after save: " + savedData.getCreatedAt());
                    System.out.println("DeviceData updated_at after save: " + savedData.getUpdatedAt());
                    
                    // 验证第一次提交时 created_at 和 updated_at 应该相同
                    if (savedData.getCreatedAt() != null && savedData.getUpdatedAt() != null && 
                        savedData.getCreatedAt().equals(savedData.getUpdatedAt())) {
                        System.out.println("✓ First submission: created_at equals updated_at");
                    }
                }
            }
        }
        
        // 强制刷新到数据库
        deviceDataRepository.flush();
        System.out.println("Flushed device data to database");
        
        // 验证数据是否真的保存了
        DeviceData verifiedData = deviceDataRepository.findTopByVidOrderByCreatedAtDesc(deviceDataDto.getVid());
        if (verifiedData != null) {
            System.out.println("VERIFIED - timestamp in database: " + verifiedData.getTimestamp());
            System.out.println("VERIFIED - vstatus in database: " + verifiedData.getVstatus());
            System.out.println("VERIFIED - updated_at in database: " + verifiedData.getUpdatedAt());
            System.out.println("VERIFIED - created_at in database: " + verifiedData.getCreatedAt());
        } else {
            System.err.println("VERIFICATION FAILED - Device data not found after save!");
        }
        
        DeviceDataDto savedDto = convertToDeviceDataDto(verifiedData);
        System.out.println("=== Device data save COMPLETED ===");
        return ApiResponse.success("数据保存成功", savedDto);
    }
    
    /**
     * 检查是否是纯状态更新（只有vstatus字段有值）
     */
    private boolean isStatusOnlyUpdate(DeviceDataDto deviceDataDto) {
        return deviceDataDto.getVstatus() != null &&
               deviceDataDto.getTin() == null &&
               deviceDataDto.getTout() == null &&
               deviceDataDto.getHin() == null &&
               deviceDataDto.getHout() == null &&
               deviceDataDto.getLxin() == null &&
               deviceDataDto.getLxout() == null &&
               deviceDataDto.getBrightness() == null;
    }
    
    /**
     * 检查数据是否没有变化
     */
    private boolean isDataUnchanged(DeviceData existingData, DeviceDataDto newData) {
        return Objects.equals(existingData.getTin(), newData.getTin()) &&
               Objects.equals(existingData.getTout(), newData.getTout()) &&
               Objects.equals(existingData.getHin(), newData.getHin()) &&
               Objects.equals(existingData.getHout(), newData.getHout()) &&
               Objects.equals(existingData.getLxin(), newData.getLxin()) &&
               Objects.equals(existingData.getLxout(), newData.getLxout()) &&
               Objects.equals(existingData.getBrightness(), newData.getBrightness()) &&
               Objects.equals(existingData.getVstatus(), newData.getVstatus());
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
            System.out.println("=== Starting MQTT data processing ===");
            System.out.println("VID: " + vid);
            System.out.println("Payload: " + payload);
            
            DeviceDataDto deviceDataDto = parseDeviceDataFromJson(vid, payload);
            if (deviceDataDto != null) {
                // 输出详细的数据信息以进行调试
                System.out.println("Parsed device data - VID: " + deviceDataDto.getVid() + ", Tin: " + deviceDataDto.getTin() + ", Tout: " + deviceDataDto.getTout());
                System.out.println("Parsed device data - Hin: " + deviceDataDto.getHin() + ", Hout: " + deviceDataDto.getHout());
                System.out.println("Parsed device data - Lxin: " + deviceDataDto.getLxin() + ", Lxout: " + deviceDataDto.getLxout());
                System.out.println("Parsed device data - Brightness: " + deviceDataDto.getBrightness());
                System.out.println("Parsed device data - VStatus: " + deviceDataDto.getVstatus());
                System.out.println("Parsed device data - Timestamp: " + deviceDataDto.getTimestamp());
                
                // 设备数据主题直接保存到device_data表
                saveDeviceData(deviceDataDto);
                System.out.println("Received and saved device data for VID: " + vid + 
                    ", tin: " + deviceDataDto.getTin() + 
                    ", tout: " + deviceDataDto.getTout() + 
                    ", hin: " + deviceDataDto.getHin() + 
                    ", hout: " + deviceDataDto.getHout() + 
                    ", lxin: " + deviceDataDto.getLxin() + 
                    ", lxout: " + deviceDataDto.getLxout() + 
                    ", brightness: " + deviceDataDto.getBrightness());
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
            
            // 确保timestamp字段有值 - 优先使用设备提供的时间戳
            if (dto.getTimestamp() == null) {
                dto.setTimestamp(LocalDateTime.now());
                System.out.println("Using current time as timestamp: " + dto.getTimestamp());
            } else {
                System.out.println("Using device provided timestamp: " + dto.getTimestamp());
            }
            
            // 确保vstatus字段有值（默认为0）
            if (dto.getVstatus() == null) {
                dto.setVstatus(0);
            }
            
            System.out.println("Parsed device data - VID: " + dto.getVid() + ", timestamp: " + dto.getTimestamp() + ", vstatus: " + dto.getVstatus());
            
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
                // deviceType字段已移除，跳过解析
            }
            
            // 支持小写"tin"格式
            if (json.contains("\"tin\"")) {
                int start = json.indexOf("\"tin\":") + 6;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 5 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTin(Double.parseDouble(value));
                }
            }
            // 支持大写"Tin"格式（来自硬件）
            else if (json.contains("\"Tin\"")) {
                int start = json.indexOf("\"Tin\":") + 6;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 5 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTin(Double.parseDouble(value));
                }
            }
            
            // 支持小写"tout"格式
            if (json.contains("\"tout\"")) {
                int start = json.indexOf("\"tout\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTout(Double.parseDouble(value));
                }
            }
            // 支持大写"Tout"格式（来自硬件）
            else if (json.contains("\"Tout\"")) {
                int start = json.indexOf("\"Tout\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setTout(Double.parseDouble(value));
                }
            }
            
            // 支持小写"hin"格式
            if (json.contains("\"hin\"")) {
                int start = json.indexOf("\"hin\":") + 6;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 5 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setHin(Integer.parseInt(value));
                }
            }
            // 支持大写"Hin"格式（来自硬件）
            else if (json.contains("\"Hin\"")) {
                int start = json.indexOf("\"Hin\":") + 6;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 5 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setHin(Integer.parseInt(value));
                }
            }
            
            // 支持小写"hout"格式
            if (json.contains("\"hout\"")) {
                int start = json.indexOf("\"hout\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setHout(Integer.parseInt(value));
                }
            }
            // 支持大写"Hout"格式（来自硬件）
            else if (json.contains("\"Hout\"")) {
                int start = json.indexOf("\"Hout\":") + 7;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 6 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setHout(Integer.parseInt(value));
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
            
            // 支持小写"lxout"格式
            if (json.contains("\"lxout\"")) {
                int start = json.indexOf("\"lxout\":") + 8;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 7 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setLxout(Integer.parseInt(value));
                }
            }
            // 支持大写"LXout"格式（来自硬件）
            else if (json.contains("\"LXout\"")) {
                int start = json.indexOf("\"LXout\":") + 8;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 7 && end > start) {
                    String value = json.substring(start, end).trim();
                    dto.setLxout(Integer.parseInt(value));
                }
            }
            
            if (json.contains("\"pid\"")) {
                // pid字段已移除，跳过解析
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
            
            // 解析时间戳字段
            if (json.contains("\"timestamp\"")) {
                int start = json.indexOf("\"timestamp\":") + 12;
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (start > 11 && end > start) {
                    String value = json.substring(start, end).trim();
                    // 移除引号
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    try {
                        // 解析ISO 8601格式的时间戳
                        dto.setTimestamp(LocalDateTime.parse(value));
                        System.out.println("Manually parsed timestamp: " + dto.getTimestamp());
                    } catch (Exception e) {
                        System.err.println("Error parsing timestamp: " + value + ", using current time");
                        dto.setTimestamp(LocalDateTime.now());
                    }
                }
            }
            
            if (json.contains("\"battery\"")) {
                int start = json.indexOf("\"battery\":") + 10;
                int end = json.indexOf(",", start);
                // battery字段已移除，跳过解析
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
        
        // 温度数据
        dto.setTin(deviceData.getTin());
        dto.setTout(deviceData.getTout());
        
        // 湿度数据
        dto.setHin(deviceData.getHin());
        dto.setHout(deviceData.getHout());
        
        // 光照数据
        dto.setLxin(deviceData.getLxin());
        dto.setLxout(deviceData.getLxout());
        
        // 亮度调节
        dto.setBrightness(deviceData.getBrightness());
        
        // 设备状态 - 前端期望vStatus字段名
        dto.setVstatus(deviceData.getVstatus());
        
        // 风机速度字段 - 数据库中不存在，设为null
        dto.setSpeedM1(null);
        dto.setSpeedM2(null);
        
        // 时间戳 - 使用设备数据的时间戳或创建时间
        if (deviceData.getTimestamp() != null) {
            dto.setTimestamp(deviceData.getTimestamp());
        } else {
            dto.setTimestamp(deviceData.getCreatedAt());
        }
        
        // 创建时间
        dto.setCreatedAt(deviceData.getCreatedAt());
        
        return dto;
    }
    
    /**
     * 将DeviceDataHistory转换为DeviceDataDto
     * 按照前端要求的字段格式：update_at, tin, tout, lxin, vStatus
     */
    private DeviceDataDto convertHistoryToDeviceDataDto(DeviceDataHistory historyData) {
        DeviceDataDto dto = new DeviceDataDto();
        dto.setId(historyData.getId());
        dto.setVid(historyData.getVid());
        
        // 温度数据
        dto.setTin(historyData.getTin());
        dto.setTout(historyData.getTout());
        
        // 湿度数据
        dto.setHin(historyData.getHin());
        dto.setHout(historyData.getHout());
        
        // 光照数据
        dto.setLxin(historyData.getLxin());
        dto.setLxout(historyData.getLxout());
        
        // 亮度调节
        dto.setBrightness(historyData.getBrightness());
        
        // 设备状态 - 前端期望vStatus字段名
        dto.setVstatus(historyData.getVstatus());
        
        // 风机速度字段 - 数据库中不存在，设为null
        dto.setSpeedM1(null);
        dto.setSpeedM2(null);
        
        // 时间戳 - 使用历史数据的更新时间
        dto.setTimestamp(historyData.getUpdatedAt());
        
        // 创建时间 - 历史数据没有创建时间，使用更新时间
        dto.setCreatedAt(historyData.getUpdatedAt());
        
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
            // 使用lxin字段作为光照值
            Integer lightValue = data.getLxin();
            item.put("value", lightValue != null ? lightValue : 0);
            return item;
        }).collect(java.util.stream.Collectors.toList());

        return ApiResponse.success("获取成功", result);
    }
    
    @Override
    @Transactional
    public void updateDeviceStatus(String vid, Integer status) {
        try {
            System.out.println("=== Starting device status update for VID: " + vid + ", status: " + status + " ===");
            
            Optional<Device> deviceOpt = deviceRepository.findByVid(vid);
            System.out.println("Device lookup result: " + (deviceOpt.isPresent() ? "FOUND" : "NOT FOUND"));
            
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                System.out.println("Current device status: " + device.getStatus());
                System.out.println("Current lastHeartbeat: " + device.getLastHeartbeat());
                
                device.setStatus(status);
                device.setLastHeartbeat(LocalDateTime.now());
                device.setUpdatedAt(LocalDateTime.now());
                
                System.out.println("Setting new status: " + status);
                System.out.println("Setting new lastHeartbeat: " + LocalDateTime.now());
                
                Device savedDevice = deviceRepository.save(device);
                System.out.println("Saved device ID: " + savedDevice.getId());
                System.out.println("Updated device status in devices table for VID: " + vid + ", status: " + status);
                
                // 强制刷新到数据库
                deviceRepository.flush();
                System.out.println("Flushed device status to database for VID: " + vid);
                
                // 验证数据是否真的保存了
                Device verifiedDevice = deviceRepository.findByVid(vid).orElse(null);
                if (verifiedDevice != null) {
                    System.out.println("VERIFIED - Current status in database: " + verifiedDevice.getStatus());
                    System.out.println("VERIFIED - Current lastHeartbeat in database: " + verifiedDevice.getLastHeartbeat());
                } else {
                    System.err.println("VERIFICATION FAILED - Device not found after update!");
                }
                
                System.out.println("=== Device status update COMPLETED ===");
            } else {
                // 设备不存在，创建新设备
                System.out.println("Device not found for VID: " + vid + ", creating new device...");
                Device newDevice = new Device();
                newDevice.setVid(vid);
                newDevice.setDeviceName("Auto-Created Device " + vid);
                newDevice.setDeviceType("IoT Device");
                newDevice.setStatus(status);
                newDevice.setLastHeartbeat(LocalDateTime.now());
                newDevice.setCreatedAt(LocalDateTime.now());
                newDevice.setUpdatedAt(LocalDateTime.now());
                
                Device savedDevice = deviceRepository.save(newDevice);
                deviceRepository.flush();
                System.out.println("Created new device for VID: " + vid + ", status: " + status + ", ID: " + savedDevice.getId());
                
                // 验证新设备是否真的保存了
                Device verifiedDevice = deviceRepository.findByVid(vid).orElse(null);
                if (verifiedDevice != null) {
                    System.out.println("VERIFIED - New device status in database: " + verifiedDevice.getStatus());
                    System.out.println("VERIFIED - New device lastHeartbeat in database: " + verifiedDevice.getLastHeartbeat());
                } else {
                    System.err.println("VERIFICATION FAILED - New device not found after creation!");
                }
                
                System.out.println("=== New device creation COMPLETED ===");
            }
        } catch (Exception e) {
            System.err.println("ERROR updating device status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public DeviceData getLatestDeviceData(String vid) {
        return deviceDataRepository.findTopByVidOrderByCreatedAtDesc(vid);
    }
    
    @Override
    public ApiResponse<com.iot.fresh.dto.PaginatedResponse<DeviceDataDto>> getDeviceHistoryDataWithPagination(
            String vid, String dataType, LocalDateTime startTime, LocalDateTime endTime, Integer pageNum, Integer pageSize) {
        // 验证参数
        if (pageNum <= 0 || pageSize <= 0) {
            return ApiResponse.error("页码和页面大小必须大于0");
        }

        // 设置默认值
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30); // 默认查询最近30天
        }
        if (endTime == null) {
            endTime = LocalDateTime.now(); // 默认为当前时间
        }

        // 创建分页请求
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize, 
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "updatedAt"));

        // 使用DeviceDataHistoryService查询历史数据表
        org.springframework.data.domain.Page<DeviceDataHistory> historyDataPage = 
            deviceDataHistoryService.getHistoryDataByVidAndTimeRange(vid, startTime, endTime, pageable);

        // 如果指定了dataType，则进一步过滤结果
        List<DeviceDataHistory> filteredDataList;
        if (dataType != null && !dataType.trim().isEmpty()) {
            // 根据dataType过滤数据，例如：如果dataType是"temperature"，则只保留有温度数据的记录
            filteredDataList = historyDataPage.getContent().stream()
                .filter(data -> hasDataForType(data, dataType))
                .collect(Collectors.toList());
        } else {
            filteredDataList = historyDataPage.getContent();
        }

        // 转换为DTO并返回
        List<DeviceDataDto> dtoList = filteredDataList.stream()
                .map(this::convertHistoryToDeviceDataDto)
                .collect(Collectors.toList());

        // 由于我们进行了手动过滤，需要重新计算分页信息
         // 为了保持正确的分页行为，我们需要基于过滤后的数据重新计算分页
         int totalFiltered = dtoList.size();
         int startIndex = (pageNum - 1) * pageSize;
         int endIndex = Math.min(startIndex + pageSize, totalFiltered);
         
         List<DeviceDataDto> pagedDtoList;
         if (startIndex >= totalFiltered) {
             pagedDtoList = new ArrayList<>();
         } else {
             pagedDtoList = dtoList.subList(startIndex, endIndex);
         }

         com.iot.fresh.dto.PaginatedResponse<DeviceDataDto> paginatedResponse = new com.iot.fresh.dto.PaginatedResponse<>(
                 pagedDtoList,
                 historyDataPage.getTotalElements(), // 总数量
                 pageNum,              // 页码
                 pageSize              // 每页数量
         );

        return ApiResponse.success(paginatedResponse);
    }
    
    /**
     * 检查设备数据是否包含指定类型的数据
     * @param data 设备数据
     * @param dataType 数据类型 (如 "temperature", "humidity", "light", "battery", "status")
     * @return 是否包含该类型的数据
     */
    private boolean hasDataForType(DeviceData data, String dataType) {
        // 如果未指定数据类型或为空字符串，则包含所有数据
        if (dataType == null || dataType.trim().length() == 0) {
            return true;
        }
        
        switch (dataType.toLowerCase().trim()) {
            case "temperature":
                return data.getTin() != null || data.getTout() != null;
            case "humidity":
                return data.getHin() != null || data.getHout() != null;
            case "light":
            case "illumination":
                return data.getLxin() != null || data.getLxout() != null;
            case "battery":
                return false; // battery字段已移除
            case "status":
                return data.getVstatus() != null;
            case "speed":
            case "fan_speed":
                return false; // speedM1和speedM2字段已移除
            case "brightness":
                return data.getBrightness() != null;
            default:
                // 如果数据类型未知，返回true以包含数据
                return true;
        }
    }
    
    /**
     * 检查设备历史数据是否包含指定类型的数据
     * @param data 设备历史数据
     * @param dataType 数据类型 (如 "temperature", "humidity", "light", "battery", "status")
     * @return 是否包含该类型的数据
     */
    private boolean hasDataForType(DeviceDataHistory data, String dataType) {
        // 如果未指定数据类型或为空字符串，则包含所有数据
        if (dataType == null || dataType.trim().length() == 0) {
            return true;
        }
        
        switch (dataType.toLowerCase().trim()) {
            case "temperature":
                return data.getTin() != null || data.getTout() != null;
            case "humidity":
                return data.getHin() != null || data.getHout() != null;
            case "light":
            case "illumination":
                return data.getLxin() != null || data.getLxout() != null;
            case "battery":
                return false; // battery字段已移除
            case "status":
                return data.getVstatus() != null;
            case "speed":
            case "fan_speed":
                return false; // speedM1和speedM2字段已移除
            case "brightness":
                return data.getBrightness() != null;
            default:
                // 如果数据类型未知，返回true以包含数据
                return true;
        }
    }
    
    @Override
    public Map<String, Object> getDeviceDataStatistics(String vid, LocalDateTime startTime, LocalDateTime endTime) {
        List<DeviceData> deviceDataList;
        
        if (vid != null && !vid.trim().isEmpty()) {
            // 查询特定设备的数据
            deviceDataList = deviceDataRepository.findByVidAndTimeRange(vid, startTime, endTime);
        } else {
            // 查询所有设备的数据
            deviceDataList = deviceDataRepository.findByTimeRangeWithNoPagination(startTime, endTime);
        }
        
        // 初始化统计结果
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        if (deviceDataList.isEmpty()) {
            statistics.put("totalRecords", 0);
            statistics.put("avgTemp", 0.0);
            statistics.put("maxTemp", 0.0);
            statistics.put("minTemp", 0.0);
            statistics.put("avgHumidity", 0.0);
            statistics.put("avgLight", 0.0);
            statistics.put("detail", java.util.Collections.emptyList());
            return statistics;
        }
        
        // 计算统计数据
        long totalRecords = deviceDataList.size();
        
        // 计算平均温度
        double avgTemp = deviceDataList.stream()
                .filter(data -> data.getTin() != null || data.getTout() != null)
                .mapToDouble(data -> {
                    Double internalTemp = data.getTin();
                    Double externalTemp = data.getTout();
                    if (internalTemp != null && externalTemp != null) {
                        return (internalTemp + externalTemp) / 2.0;
                    } else if (internalTemp != null) {
                        return internalTemp;
                    } else if (externalTemp != null) {
                        return externalTemp;
                    } else {
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
        
        // 计算最高温度
        double maxTemp = deviceDataList.stream()
                .filter(data -> data.getTin() != null || data.getTout() != null)
                .mapToDouble(data -> {
                    Double internalTemp = data.getTin();
                    Double externalTemp = data.getTout();
                    if (internalTemp != null && externalTemp != null) {
                        return Math.max(internalTemp, externalTemp);
                    } else if (internalTemp != null) {
                        return internalTemp;
                    } else if (externalTemp != null) {
                        return externalTemp;
                    } else {
                        return 0.0;
                    }
                })
                .max()
                .orElse(0.0);
        
        // 计算最低温度
        double minTemp = deviceDataList.stream()
                .filter(data -> data.getTin() != null || data.getTout() != null)
                .mapToDouble(data -> {
                    Double internalTemp = data.getTin();
                    Double externalTemp = data.getTout();
                    if (internalTemp != null && externalTemp != null) {
                        return Math.min(internalTemp, externalTemp);
                    } else if (internalTemp != null) {
                        return internalTemp;
                    } else if (externalTemp != null) {
                        return externalTemp;
                    } else {
                        return 0.0;
                    }
                })
                .min()
                .orElse(0.0);
        
        // 计算平均湿度
        double avgHumidity = deviceDataList.stream()
                .filter(data -> data.getHin() != null || data.getHout() != null)
                .mapToDouble(data -> {
                    Integer internalHumidity = data.getHin();
                    Integer externalHumidity = data.getHout();
                    if (internalHumidity != null && externalHumidity != null) {
                        return (internalHumidity + externalHumidity) / 2.0;
                    } else if (internalHumidity != null) {
                        return internalHumidity.doubleValue();
                    } else if (externalHumidity != null) {
                        return externalHumidity.doubleValue();
                    } else {
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
        
        // 计算平均光照
        double avgLight = deviceDataList.stream()
                .filter(data -> data.getLxin() != null)
                .mapToInt(DeviceData::getLxin)
                .average()
                .orElse(0.0);
        
        // 设置统计结果
        statistics.put("totalRecords", totalRecords);
        statistics.put("avgTemp", avgTemp);
        statistics.put("maxTemp", maxTemp);
        statistics.put("minTemp", minTemp);
        statistics.put("avgHumidity", avgHumidity);
        statistics.put("avgLight", avgLight);
        
        // 构建详细统计信息
        List<Map<String, Object>> details = new java.util.ArrayList<>();
        if (vid != null && !vid.trim().isEmpty()) {
            // 如果是特定设备，则添加该设备的详细统计
            Map<String, Object> deviceDetail = new java.util.HashMap<>();
            deviceDetail.put("deviceName", "Unknown Device"); // deviceName字段已移除
            deviceDetail.put("vid", vid);
            deviceDetail.put("avgTemp", avgTemp);
            deviceDetail.put("maxTemp", maxTemp);
            deviceDetail.put("minTemp", minTemp);
            deviceDetail.put("avgHumidity", avgHumidity);
            deviceDetail.put("avgLight", avgLight);
            deviceDetail.put("recordCount", totalRecords);
            deviceDetail.put("timeRange", startTime.toString() + " ~ " + endTime.toString());
            
            details.add(deviceDetail);
        } else {
            // 如果是所有设备，则按设备分组统计
            java.util.Map<String, List<DeviceData>> groupedByDevice = deviceDataList.stream()
                    .collect(java.util.stream.Collectors.groupingBy(DeviceData::getVid));
            
            for (java.util.Map.Entry<String, List<DeviceData>> entry : groupedByDevice.entrySet()) {
                String deviceVid = entry.getKey();
                List<DeviceData> deviceList = entry.getValue();
                
                // 查找设备名称
                String deviceName = "Unknown Device";
                Optional<Device> deviceOpt = deviceRepository.findByVid(deviceVid);
                if (deviceOpt.isPresent()) {
                    deviceName = deviceOpt.get().getDeviceName();
                }
                
                // 计算该设备的统计信息
                double deviceAvgTemp = deviceList.stream()
                        .filter(data -> data.getTin() != null || data.getTout() != null)
                        .mapToDouble(data -> {
                            Double internalTemp = data.getTin();
                            Double externalTemp = data.getTout();
                            if (internalTemp != null && externalTemp != null) {
                                return (internalTemp + externalTemp) / 2.0;
                            } else if (internalTemp != null) {
                                return internalTemp;
                            } else if (externalTemp != null) {
                                return externalTemp;
                            } else {
                                return 0.0;
                            }
                        })
                        .average()
                        .orElse(0.0);
                
                double deviceMaxTemp = deviceList.stream()
                        .filter(data -> data.getTin() != null || data.getTout() != null)
                        .mapToDouble(data -> {
                            Double internalTemp = data.getTin();
                            Double externalTemp = data.getTout();
                            if (internalTemp != null && externalTemp != null) {
                                return Math.max(internalTemp, externalTemp);
                            } else if (internalTemp != null) {
                                return internalTemp;
                            } else if (externalTemp != null) {
                                return externalTemp;
                            } else {
                                return 0.0;
                            }
                        })
                        .max()
                        .orElse(0.0);
                
                double deviceMinTemp = deviceList.stream()
                        .filter(data -> data.getTin() != null || data.getTout() != null)
                        .mapToDouble(data -> {
                            Double internalTemp = data.getTin();
                            Double externalTemp = data.getTout();
                            if (internalTemp != null && externalTemp != null) {
                                return Math.min(internalTemp, externalTemp);
                            } else if (internalTemp != null) {
                                return internalTemp;
                            } else if (externalTemp != null) {
                                return externalTemp;
                            } else {
                                return 0.0;
                            }
                        })
                        .min()
                        .orElse(0.0);
                
                double deviceAvgHumidity = deviceList.stream()
                        .filter(data -> data.getHin() != null || data.getHout() != null)
                        .mapToDouble(data -> {
                            Integer internalHumidity = data.getHin();
                            Integer externalHumidity = data.getHout();
                            if (internalHumidity != null && externalHumidity != null) {
                                return (internalHumidity + externalHumidity) / 2.0;
                            } else if (internalHumidity != null) {
                                return internalHumidity.doubleValue();
                            } else if (externalHumidity != null) {
                                return externalHumidity.doubleValue();
                            } else {
                                return 0.0;
                            }
                        })
                        .average()
                        .orElse(0.0);
                
                double deviceAvgLight = deviceList.stream()
                        .filter(data -> data.getLxin() != null)
                        .mapToInt(DeviceData::getLxin)
                        .average()
                        .orElse(0.0);
                
                Map<String, Object> deviceDetail = new java.util.HashMap<>();
                deviceDetail.put("deviceName", deviceName);
                deviceDetail.put("vid", deviceVid);
                deviceDetail.put("avgTemp", deviceAvgTemp);
                deviceDetail.put("maxTemp", deviceMaxTemp);
                deviceDetail.put("minTemp", deviceMinTemp);
                deviceDetail.put("avgHumidity", deviceAvgHumidity);
                deviceDetail.put("avgLight", deviceAvgLight);
                deviceDetail.put("recordCount", (long) deviceList.size());
                deviceDetail.put("timeRange", startTime.toString() + " ~ " + endTime.toString());
                
                details.add(deviceDetail);
            }
        }
        
        statistics.put("detail", details);
        
        return statistics;
    }
    
    @Override
    public void saveStatusOnlyData(String vid, Integer status) {
        try {
            System.out.println("=== Starting status-only data save for VID: " + vid + ", status: " + status + " ===");
            
            // 查找设备的最新数据记录
            DeviceData latestData = deviceDataRepository.findTopByVidOrderByCreatedAtDesc(vid);
            
            if (latestData != null) {
                System.out.println("Found existing data record - current vstatus: " + latestData.getVstatus());
                
                // 检查状态是否发生变化
                if (Objects.equals(latestData.getVstatus(), status)) {
                    // 状态没有变化，只更新更新时间字段
                    System.out.println("Status unchanged (" + status + "), updating only updated_at field");
                    
                    // 保存原始的时间戳和状态值，避免被覆盖
                    LocalDateTime originalTimestamp = latestData.getTimestamp();
                    Integer originalVstatus = latestData.getVstatus();
                    
                    latestData.setUpdatedAt(LocalDateTime.now());
                    DeviceData savedData = deviceDataRepository.save(latestData);
                    
                    // 恢复原始的时间戳和状态值
                    savedData.setTimestamp(originalTimestamp);
                    savedData.setVstatus(originalVstatus);
                    
                    System.out.println("Updated only updated_at field: " + savedData.getUpdatedAt());
                    System.out.println("Preserved timestamp: " + savedData.getTimestamp());
                    System.out.println("Preserved vstatus: " + savedData.getVstatus());
                } else {
                    // 状态发生变化，更新状态和更新时间
                    System.out.println("Status changed from " + latestData.getVstatus() + " to " + status + ", updating vstatus and updated_at");
                    
                    // 保存原始的时间戳，避免被覆盖
                    LocalDateTime originalTimestamp = latestData.getTimestamp();
                    
                    latestData.setVstatus(status);
                    latestData.setUpdatedAt(LocalDateTime.now());
                    DeviceData savedData = deviceDataRepository.save(latestData);
                    
                    // 恢复原始的时间戳
                    savedData.setTimestamp(originalTimestamp);
                    
                    System.out.println("Updated vstatus: " + savedData.getVstatus());
                    System.out.println("Updated updated_at: " + savedData.getUpdatedAt());
                    System.out.println("Preserved timestamp: " + savedData.getTimestamp());
                }
            } else {
                // 没有找到现有记录，创建新的状态记录
                System.out.println("No existing data record found, creating new status-only record");
                
                DeviceData deviceData = new DeviceData();
                deviceData.setVid(vid);
                deviceData.setVstatus(status);
                deviceData.setTimestamp(LocalDateTime.now()); // 使用当前时间作为时间戳
                
                // 其他传感器数据字段设为null
                deviceData.setTin(null);
                deviceData.setTout(null);
                deviceData.setHin(null);
                deviceData.setHout(null);
                deviceData.setLxin(null);
                deviceData.setLxout(null);
                deviceData.setBrightness(null);
                
                System.out.println("Status-only data before save - VID: " + deviceData.getVid() + ", vstatus: " + deviceData.getVstatus() + ", timestamp: " + deviceData.getTimestamp());
                
                DeviceData savedData = deviceDataRepository.save(deviceData);
                System.out.println("Status-only data after save - vstatus: " + savedData.getVstatus() + ", timestamp: " + savedData.getTimestamp());
            }
            
            // 强制刷新到数据库
            deviceDataRepository.flush();
            System.out.println("Flushed status-only data to database");
            
            // 验证数据是否真的保存了
            DeviceData verifiedData = deviceDataRepository.findTopByVidOrderByCreatedAtDesc(vid);
            if (verifiedData != null) {
                System.out.println("VERIFIED - vstatus in database: " + verifiedData.getVstatus());
                System.out.println("VERIFIED - timestamp in database: " + verifiedData.getTimestamp());
                System.out.println("VERIFIED - updated_at in database: " + verifiedData.getUpdatedAt());
            } else {
                System.err.println("VERIFICATION FAILED - Status-only data not found after save!");
            }
            
            System.out.println("=== Status-only data save COMPLETED ===");
        } catch (Exception e) {
            System.err.println("ERROR saving status-only data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新设备数据表中的状态信息（在同一个设备记录上）
     */
    @Override
    @Transactional
    public void updateDeviceDataStatus(String vid, Integer status) {
        try {
            System.out.println("=== Starting device data status update for VID: " + vid + ", status: " + status + " ===");
            
            // 查找设备的最新数据记录
            DeviceData latestData = deviceDataRepository.findTopByVidOrderByCreatedAtDesc(vid);
            
            if (latestData != null) {
                System.out.println("Found existing data record - current vstatus: " + latestData.getVstatus());
                
                // 无论状态是否变化，每次提交都要更新 updated_at 为当前时间
                if (Objects.equals(latestData.getVstatus(), status)) {
                    // 状态没有变化，只更新更新时间字段
                    System.out.println("Status unchanged (" + status + "), updating only updated_at field");
                } else {
                    // 状态发生变化，更新状态和更新时间
                    System.out.println("Status changed from " + latestData.getVstatus() + " to " + status + ", updating vstatus and updated_at");
                    latestData.setVstatus(status);
                }
                
                // 每次提交都要更新 updated_at 为当前时间
                latestData.setUpdatedAt(LocalDateTime.now());
                DeviceData savedData = deviceDataRepository.save(latestData);
                
                System.out.println("Updated updated_at: " + savedData.getUpdatedAt());
                if (!Objects.equals(latestData.getVstatus(), status)) {
                    System.out.println("Updated vstatus: " + savedData.getVstatus());
                }
                
                // 强制刷新到数据库
                deviceDataRepository.flush();
                System.out.println("Flushed device data status update to database");
                
                // 验证数据是否真的保存了
                DeviceData verifiedData = deviceDataRepository.findTopByVidOrderByCreatedAtDesc(vid);
                if (verifiedData != null) {
                    System.out.println("VERIFIED - vstatus in database: " + verifiedData.getVstatus());
                    System.out.println("VERIFIED - updated_at in database: " + verifiedData.getUpdatedAt());
                } else {
                    System.err.println("VERIFICATION FAILED - Device data not found after update!");
                }
                
                System.out.println("=== Device data status update COMPLETED ===");
            } else {
                System.err.println("No existing data record found for VID: " + vid + ", cannot update status");
            }
        } catch (Exception e) {
            System.err.println("ERROR updating device data status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}