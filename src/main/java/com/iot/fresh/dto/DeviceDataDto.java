package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneOffset;

@Data
public class DeviceDataDto {
    private Long id;
    private String vid;
    private String deviceName; // 设备名称
    private String deviceType;
    
    // 根据后端与硬件.md文档定义的字段
    @JsonProperty("Tin")
    private Double tin; // 内部温度
    
    @JsonProperty("Tout")
    private Double tout; // 外部温度
    
    @JsonProperty("Hin")
    private Double hin; // 内部湿度
    
    @JsonProperty("Hout")
    private Double hout; // 外部湿度
    
    @JsonProperty("LXin")
    private Integer lxin; // 内部光照
    
    @JsonProperty("light")
    private Integer light; // 光照强度
    
    @JsonProperty("pid")
    private String pid; // 产品ID
    
    @JsonProperty("VStatus")
    private Integer vstatus; // 设备状态
    
    @JsonProperty("battery")
    private Integer battery; // 电池电量
    
    @JsonProperty("brightness")
    private Integer brightness; // 亮度
    
    @JsonProperty("speedM1")
    private Integer speedM1; // 风机1速度
    
    @JsonProperty("speedM2")
    private Integer speedM2; // 风机2速度
    
    @JsonProperty("timestamp")
    private Long timestampLong; // Unix时间戳，用于接收JSON中的时间戳
    
    // 从Unix时间戳转换得到的LocalDateTime
    private LocalDateTime timestamp; // 转换后的时间
    
    // 在设置timestampLong时自动转换为LocalDateTime
    public void setTimestampLong(Long timestampLong) {
        this.timestampLong = timestampLong;
        if (timestampLong != null) {
            this.timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestampLong), 
                ZoneOffset.UTC
            );
        }
    }
    
    // 提供timestamp的getter，但忽略它以避免与timestampLong冲突
    @JsonIgnore
    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }
    
    // 提供一个方法来获取转换后的时间，避免Jackson混淆
    public LocalDateTime getConvertedTimestamp() {
        return this.timestamp;
    }
}