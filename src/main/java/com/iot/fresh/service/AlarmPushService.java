package com.iot.fresh.service;

import com.iot.fresh.dto.PriorityAlarmMessage;
import com.iot.fresh.entity.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlarmPushService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public void sendPriorityAlarm(Alarm alarm) {
        PriorityAlarmMessage message = new PriorityAlarmMessage();
        message.setAlarmInfo(convertToAlarmInfo(alarm));
        messagingTemplate.convertAndSend("/topic/priority-alarms", message);
    }
    
    private PriorityAlarmMessage.AlarmInfo convertToAlarmInfo(Alarm alarm) {
        PriorityAlarmMessage.AlarmInfo alarmInfo = new PriorityAlarmMessage.AlarmInfo();
        alarmInfo.setId(alarm.getId());
        alarmInfo.setDeviceId(alarm.getVid());
        alarmInfo.setDeviceName(alarm.getDeviceName());
        alarmInfo.setAlarmType(alarm.getAlarmType());
        alarmInfo.setLevel(alarm.getAlarmLevel());
        alarmInfo.setAlarmContent(alarm.getMessage());
        alarmInfo.setStatus(alarm.getStatus());
        alarmInfo.setTimestamp(alarm.getCreatedAt() != null ? alarm.getCreatedAt().toString() : null);
        return alarmInfo;
    }
}