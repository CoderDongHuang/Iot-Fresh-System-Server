package com.iot.fresh.dto;

import lombok.Data;

@Data
public class DeviceStatusStatsDto {
    private Integer totalDevices;
    private Integer onlineDevices;
    private Integer offlineDevices;
    private Integer faultDevices;
}