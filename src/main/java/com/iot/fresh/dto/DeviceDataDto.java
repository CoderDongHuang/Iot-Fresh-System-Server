package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDataDto {
    private Long id;
    private String vid;
    private String deviceType;
    private Double tin; // 内部温度
    private Double tout; // 外部温度
    private Integer lxin; // 内部光照
    private String pid; // 产品ID
    private Integer vstatus; // 设备状态
    private Integer battery; // 电池电量
    private Integer brightness; // 亮度
    private Integer speedM1; // 风机1速度
    private Integer speedM2; // 风机2速度
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}