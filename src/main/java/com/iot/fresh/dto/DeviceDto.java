package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    // 根据API规范提供多种时间字段格式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnline_time;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime last_heartbeat;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime create_time;
    
    private LocalDateTime updatedAt;
    private DeviceCurrentDataDto currentData;
}