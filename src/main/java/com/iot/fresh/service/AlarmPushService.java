package com.iot.fresh.service;

import com.iot.fresh.entity.Alarm;
import com.iot.fresh.websocket.WebSocketEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlarmPushService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void sendPriorityAlarm(Alarm alarm) {
        try {
            // 构建前端需要的消息格式
            Map<String, Object> message = new HashMap<>();
            message.put("type", "new_alarm");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", alarm.getId());
            data.put("deviceName", alarm.getDeviceName());
            
            // 映射报警级别：将后端级别映射到前端三个级别
            String frontendLevel = convertToFrontendLevel(alarm.getAlarmLevel());
            data.put("level", frontendLevel);
            
            data.put("alarmContent", alarm.getMessage());
            data.put("timestamp", alarm.getCreatedAt() != null ? alarm.getCreatedAt().toString() : null);
            
            // 映射状态值以匹配前端格式
            String status = alarm.getStatus();
            if ("active".equals(status)) {
                status = "active";
            } else if ("resolved".equals(status)) {
                status = "resolved";
            }
            data.put("status", status);
            
            message.put("data", data);
            
            // 转换为JSON字符串并发送到所有WebSocket客户端
            String jsonMessage = objectMapper.writeValueAsString(message);
            WebSocketEndpoint.sendMessageToAll(jsonMessage);
            
            System.out.println("推送新报警消息: " + jsonMessage);
        } catch (Exception e) {
            System.err.println("推送报警消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
      * 将后端报警级别映射到前端三个级别
      */
     private String convertToFrontendLevel(String backendLevel) {
         if (backendLevel == null) {
             return "low";
         }
         
         switch (backendLevel.toLowerCase()) {
             case "high":
                 return "high";
             case "medium":
                 return "medium";
             case "low":
                 return "low";
             default:
                 return "low";
         }
     }
 }