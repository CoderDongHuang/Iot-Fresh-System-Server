package com.iot.fresh.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.service.AlarmService;
import com.iot.fresh.service.DataService;
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
                        break;
                    case "alarm":
                        // 处理报警数据
                        processAlarmData(vid, payload);
                        break;
                    case "status":
                        // 处理状态数据 - 直接更新设备状态
                        updateDeviceStatusDirectly(vid, payload);
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
            // 如果JSON中没有VID，使用传入的VID
            if (alarmData.getVid() == null || alarmData.getVid().isEmpty()) {
                alarmData.setVid(vid);
            }
            
            // 调用报警处理服务
            alarmService.processAlarm(alarmData);
            
            System.out.println("Received alarm data for VID: " + vid + ", Type: " + alarmData.getAlarmType());
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
            // 解析状态更新消息
            Integer status = parseStatusFromPayload(payload);
            if (status != null) {
                // 更新设备状态到devices表
                dataService.updateDeviceStatus(vid, status);
                System.out.println("Directly updated device status for VID: " + vid + ", status: " + status);
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
            // 同时支持status和vstatus字段
            Object statusObj = jsonMap.get("status");
            if (statusObj == null) {
                statusObj = jsonMap.get("vstatus");
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
}