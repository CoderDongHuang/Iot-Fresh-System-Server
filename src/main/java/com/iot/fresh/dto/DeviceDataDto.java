package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DeviceDataDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 前端期望的字段格式
    private Long id;
    private String vid;
    
    // 温度数据
    private Double tin; // 内部温度
    private Double tout; // 外部温度
    
    // 湿度数据
    private Integer hin; // 内部湿度
    private Integer hout; // 外部湿度
    
    // 光照数据
    private Integer lxin; // 内部光照
    private Integer lxout; // 外部光照
    
    // 亮度调节
    private Integer brightness; // 亮度
    
    // 风机速度
    private Integer speedM1; // 速度1
    private Integer speedM2; // 速度2
    
    // 设备状态 - 前端期望vStatus字段名
    @JsonProperty("vStatus")
    private Integer vstatus; // 设备状态
    
    // 时间戳 - 前端期望字符串格式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp; // 数据时间戳
    
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}