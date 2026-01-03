package com.iot.fresh.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmDto {
    private Long deviceId;
    private String vid;
    private String deviceName;
    private String alarmType;
    private String alarmLevel;
    private String message;
    private String status;
    private LocalDateTime timestamp;
}