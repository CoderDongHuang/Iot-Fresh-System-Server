package com.iot.fresh.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceDetailDto {
    private String vid;
    private String deviceName;
    private String deviceType;
    private Integer status;
    private String location;
    private String contactPhone;
    private String description;
    private LocalDateTime lastOnlineTime;
    private LocalDateTime createTime;
    private String remarks;
    private DeviceCurrentDataDto currentData;
}