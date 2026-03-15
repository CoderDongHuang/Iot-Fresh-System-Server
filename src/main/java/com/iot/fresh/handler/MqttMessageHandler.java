package com.iot.fresh.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.service.AlarmService;
import com.iot.fresh.service.DataService;
import com.iot.fresh.service.DeviceDataHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

@Component
public class MqttMessageHandler {

    @Autowired
    private DataService dataService;
    
    @Autowired
    private AlarmService alarmService;
    
    @Autowired
    private DeviceDataHistoryService deviceDataHistoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(@Payload String payload, @Headers MessageHeaders headers) {
        // 获取主题信息
        String topic = (String) headers.get("mqtt_receivedTopic");
        
        if (topic != null) {
            // 提取设备ID (从类似 "device/V001/data" 的主题中)
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 3) {
                String vid = topicParts[1]; // 设备ID
                String messageType = topicParts[2]; // 消息类型 (data, alarm, status, rfid)

                switch (messageType) {
                    case "data":
                        // 处理设备数据
                        dataService.processDeviceDataFromMqtt(vid, payload);
                        
                        // 新增：同时保存到历史数据表
                        saveToDeviceDataHistory(vid, payload);
                        break;
                    case "alarm":
                        // 处理报警数据
                        processAlarmData(vid, payload);
                        break;
                    case "status":
                        // 处理状态数据 - 直接更新设备状态
                        updateDeviceStatusDirectly(vid, payload);
                        
                        // 新增：同时保存到历史数据表
                        saveStatusToDeviceDataHistory(vid, payload);
                        break;
                    case "rfid":
                        // 处理RFID数据
                        dataService.processDeviceDataFromMqtt(vid, payload);
                        break;
                    default:
                        System.out.println("Unknown message type: " + messageType);
                        break;
                }
            }
        }
    }

    private void processAlarmData(String vid, String payload) {
        try {
            // 解析报警数据
            AlarmDataDto alarmData = objectMapper.readValue(payload, AlarmDataDto.class);
            
            // 设置VID（从主题中获取）
            alarmData.setVid(vid);
            
            // 处理字段映射：优先使用新格式字段，如果为空则使用旧格式字段
            if (alarmData.getDeviceName() == null || alarmData.getDeviceName().isEmpty()) {
                // 如果没有设备名称，使用VID作为默认设备名称
                alarmData.setDeviceName("设备" + vid);
            }
            
            if (alarmData.getAlarmType() == null || alarmData.getAlarmType().isEmpty()) {
                // 如果没有报警类型，使用默认值
                alarmData.setAlarmType("temperature");
            }
            
            // 优先使用新格式的level字段，如果没有则使用默认值
            if (alarmData.getLevel() == null || alarmData.getLevel().isEmpty()) {
                alarmData.setLevel("medium");
            }
            
            // 优先使用新格式的alarmContent字段，如果没有则使用旧格式的message字段
            if (alarmData.getAlarmContent() == null || alarmData.getAlarmContent().isEmpty()) {
                if (alarmData.getMessage() != null && !alarmData.getMessage().isEmpty()) {
                    alarmData.setAlarmContent(alarmData.getMessage());
                } else {
                    alarmData.setAlarmContent("设备" + vid + "发生报警");
                }
            }
            
            // 设置默认状态
            if (alarmData.getStatus() == null || alarmData.getStatus().isEmpty()) {
                alarmData.setStatus("active");
            }
            
            // 调用报警处理服务
            alarmService.processAlarm(alarmData);
            
            System.out.println("Received alarm data for VID: " + vid + ", Device: " + alarmData.getDeviceName() + ", Level: " + alarmData.getLevel());
        } catch (Exception e) {
            System.err.println("Error processing alarm data: " + e.getMessage());
            e.printStackTrace();
            
            // 如果Jackson解析失败，回退到手动解析
            try {
                AlarmDataDto alarmData = parseAlarmDataManually(vid, payload);
                if (alarmData != null) {
                    alarmService.processAlarm(alarmData);
                    System.out.println("Received alarm data (manual parse) for VID: " + vid);
                }
            } catch (Exception ex) {
                System.err.println("Error parsing alarm data manually: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // 手动解析报警数据作为备用
    /**
     * 直接更新设备状态
     */
    private void updateDeviceStatusDirectly(String vid, String payload) {
        try {
            System.out.println("Processing status update for VID: " + vid + ", payload: " + payload);
            
            // 解析状态更新消息
            Integer status = parseStatusFromPayload(payload);
            if (status != null) {
                System.out.println("Parsed VStatus: " + status);
                
                // 1. 更新设备状态到devices表
                dataService.updateDeviceStatus(vid, status);
                System.out.println("Updated device status in devices table for VID: " + vid + ", status: " + status);
                
                // 2. 更新设备数据表中的状态信息（在同一个设备记录上）
                dataService.updateDeviceDataStatus(vid, status);
                System.out.println("Updated device status in device_data table for VID: " + vid + ", status: " + status);
                
            } else {
                System.err.println("Failed to parse status from payload: " + payload);
            }
        } catch (Exception e) {
            System.err.println("Error updating device status directly: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从载荷中解析状态值
     */
    private Integer parseStatusFromPayload(String payload) {
        try {
            // 尝试使用Jackson解析
            java.util.Map<String, Object> jsonMap = objectMapper.readValue(payload, java.util.Map.class);
            // 支持多种状态字段格式：status, vstatus, VStatus
            Object statusObj = jsonMap.get("status");
            if (statusObj == null) {
                statusObj = jsonMap.get("vstatus");
            }
            if (statusObj == null) {
                statusObj = jsonMap.get("VStatus");
            }
            
            if (statusObj != null) {
                if (statusObj instanceof Number) {
                    return ((Number) statusObj).intValue();
                } else if (statusObj instanceof String) {
                    return Integer.parseInt((String) statusObj);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing status with Jackson: " + e.getMessage());
            // 回退到手动解析 - 同时支持status和vstatus
            try {
                // 先尝试解析status
                if (payload.contains("\"status\"")) {
                    int start = payload.indexOf("\"status\":") + 9;
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.indexOf("}", start);
                    if (start > 8 && end > start) {
                        String value = payload.substring(start, end).trim();
                        return Integer.parseInt(value);
                    }
                }
                // 如果没有status，尝试解析vstatus
                else if (payload.contains("\"vstatus\"")) {
                    int start = payload.indexOf("\"vstatus\":") + 10;
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.indexOf("}", start);
                    if (start > 9 && end > start) {
                        String value = payload.substring(start, end).trim();
                        return Integer.parseInt(value);
                    }
                }
                // 如果没有vstatus，尝试解析VStatus
                else if (payload.contains("\"VStatus\"")) {
                    int start = payload.indexOf("\"VStatus\":") + 10;
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.indexOf("}", start);
                    if (start > 9 && end > start) {
                        String value = payload.substring(start, end).trim();
                        return Integer.parseInt(value);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error parsing status manually: " + ex.getMessage());
            }
        }
        return null;
    }

    private AlarmDataDto parseAlarmDataManually(String vid, String json) {
        try {
            AlarmDataDto dto = new AlarmDataDto();
            dto.setVid(vid);
            
            // 解析报警类型
            if (json.contains("\"alarmType\"")) {
                int start = json.indexOf("\"alarmType\":\"") + 15;
                int end = json.indexOf("\"", start);
                if (start > 14 && end > start) {
                    dto.setAlarmType(json.substring(start, end));
                }
            }
            
            // 解析消息
            if (json.contains("\"message\"")) {
                int start = json.indexOf("\"message\":\"") + 11;
                int end = json.indexOf("\"", start);
                if (start > 10 && end > start) {
                    dto.setMessage(json.substring(start, end));
                }
            }
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error parsing alarm JSON manually: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 新增：保存设备数据到历史数据表
     * 每次都要创建新记录，状态和设备表保持一致
     */
    private void saveToDeviceDataHistory(String vid, String payload) {
        try {
            System.out.println("=== Starting save to device data history ===");
            System.out.println("VID: " + vid + ", Payload: " + payload);
            
            // 解析设备数据
            DeviceDataDto deviceDataDto = parseDeviceDataFromJson(vid, payload);
            if (deviceDataDto != null) {
                System.out.println("Parsed device data for history - Tin: " + deviceDataDto.getTin() + ", Tout: " + deviceDataDto.getTout());
                
                // 调用历史数据服务保存
                deviceDataHistoryService.saveDeviceDataHistory(deviceDataDto);
                System.out.println("Saved device data to history table");
            } else {
                System.err.println("Failed to parse device data for history table");
            }
            
            System.out.println("=== Save to device data history COMPLETED ===");
        } catch (Exception e) {
            System.err.println("ERROR saving to device data history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 新增：保存状态数据到历史数据表
     * 创建新记录，数据和上一条保持一致，状态更改
     */
    private void saveStatusToDeviceDataHistory(String vid, String payload) {
        try {
            System.out.println("=== Starting save status to device data history ===");
            System.out.println("VID: " + vid + ", Payload: " + payload);
            
            // 解析状态
            Integer status = parseStatusFromPayload(payload);
            if (status != null) {
                System.out.println("Parsed status for history: " + status);
                
                // 调用历史数据服务保存状态
                deviceDataHistoryService.saveStatusHistory(vid, status);
                System.out.println("Saved status to history table");
            } else {
                System.err.println("Failed to parse status for history table");
            }
            
            System.out.println("=== Save status to device data history COMPLETED ===");
        } catch (Exception e) {
            System.err.println("ERROR saving status to device data history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 解析设备数据JSON
     */
    private DeviceDataDto parseDeviceDataFromJson(String vid, String payload) {
        try {
            // 使用现有的DataService中的解析逻辑
            // 这里简化处理，实际应该复用DataService中的解析方法
            DeviceDataDto dto = new DeviceDataDto();
            dto.setVid(vid);
            
            // 解析JSON数据
            java.util.Map<String, Object> jsonMap = objectMapper.readValue(payload, java.util.Map.class);
            
            // 解析温度数据
            if (jsonMap.containsKey("Tin")) {
                dto.setTin(convertToDouble(jsonMap.get("Tin")));
            }
            if (jsonMap.containsKey("Tout")) {
                dto.setTout(convertToDouble(jsonMap.get("Tout")));
            }
            
            // 解析湿度数据
            if (jsonMap.containsKey("Hin")) {
                dto.setHin(convertToInteger(jsonMap.get("Hin")));
            }
            if (jsonMap.containsKey("Hout")) {
                dto.setHout(convertToInteger(jsonMap.get("Hout")));
            }
            
            // 解析光照数据
            if (jsonMap.containsKey("LXin")) {
                dto.setLxin(convertToInteger(jsonMap.get("LXin")));
            }
            if (jsonMap.containsKey("LXout")) {
                dto.setLxout(convertToInteger(jsonMap.get("LXout")));
            }
            
            // 解析亮度数据
            if (jsonMap.containsKey("brightness")) {
                dto.setBrightness(convertToInteger(jsonMap.get("brightness")));
            }
            
            System.out.println("Parsed device data - Tin: " + dto.getTin() + ", Tout: " + dto.getTout());
            return dto;
            
        } catch (Exception e) {
            System.err.println("Error parsing device data JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 转换为Double类型
     */
    private Double convertToDouble(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 转换为Integer类型
     */
    private Integer convertToInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}