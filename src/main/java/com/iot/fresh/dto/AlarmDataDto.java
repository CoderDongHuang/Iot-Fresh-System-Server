package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AlarmDataDto {
    @JsonProperty("vid")
    private String vid;
    
    // 新格式字段映射
    @JsonProperty("deviceName")
    private String deviceName; // 设备名称 -> device_name
    
    @JsonProperty("alarmType")
    private String alarmType;  // 报警类型 -> alarm_type
    
    @JsonProperty("level")
    private String level;      // 报警级别 -> alarm_level
    
    @JsonProperty("alarmContent")
    private String alarmContent; // 报警内容 -> message
    
    @JsonProperty("status")
    private String status;     // 报警状态 -> status
    
    @JsonProperty("timestamp")
    private String timestamp;  // 时间戳 -> created_at
    
    // 兼容旧格式的字段
    @JsonProperty("type")
    private String type;  // 旧格式报警类型 -> alarm_type
    
    @JsonProperty("message")
    private String message;    // 报警消息 -> message
}