package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmDataDto {
    private String vid;
    
    @JsonProperty("type")
    private String alarmType;  // 报警类型
    
    @JsonProperty("code")
    private Integer code;      // 报警代码
    
    @JsonProperty("message")
    private String message;    // 报警消息
    
    private LocalDateTime timestamp;
}