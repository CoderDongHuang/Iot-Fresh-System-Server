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
                        // 处理状态数据
                        dataService.processDeviceDataFromMqtt(vid, payload);
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