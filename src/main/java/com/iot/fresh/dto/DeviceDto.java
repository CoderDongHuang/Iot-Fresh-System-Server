package com.iot.fresh.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDto {
    private Long id;
    private String vid;
    private String deviceName;
    private String deviceType;
    private Integer status; // 0:离线, 1:在线, 2:故障, 3:维护
    private String location;
    private String contactPhone;
    private String description;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}