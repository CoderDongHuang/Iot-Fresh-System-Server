package com.iot.fresh.dto;

import lombok.Data;

@Data
public class PriorityAlarmMessage {
    private String type = "priority_alarm";
    private AlarmInfo alarmInfo;
    private boolean soundAlert = true;
    private boolean vibration = true;
    
    @Data
    public static class AlarmInfo {
        private Long id;
        private String deviceId;
        private String deviceName;
        private String alarmType;
        private String level; // high, medium, low
        private String alarmContent;
        private String status;
        private String timestamp;
    }
}