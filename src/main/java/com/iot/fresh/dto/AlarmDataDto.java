package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AlarmDataDto {
    private String vid;
    
    // 新格式字段映射
    @JsonProperty("deviceName")
    private String deviceName; // 设备名称 -> device_name
    
    @JsonProperty("level")
    private String level;      // 报警级别 -> alarm_level
    
    @JsonProperty("alarmContent")
    private String alarmContent; // 报警内容 -> message
    
    @JsonProperty("timestamp")
    private String timestamp;  // 时间戳 -> created_at
    
    @JsonProperty("status")
    private String status;     // 报警状态 -> status
    
    // 兼容旧格式的字段
    @JsonProperty("type")
    private String alarmType;  // 报警类型 -> alarm_type
    
    @JsonProperty("message")
    private String message;    // 报警消息 -> message
}