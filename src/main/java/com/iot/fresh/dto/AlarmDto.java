package com.iot.fresh.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmDto {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String alarmType; // 温度异常, 湿度异常, 设备故障
    private String alarmLevel; // high, medium, low
    private String message;
    private String status; // active, resolved, closed
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}