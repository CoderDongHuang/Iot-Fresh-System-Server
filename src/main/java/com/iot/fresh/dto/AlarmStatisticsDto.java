package com.iot.fresh.dto;

import lombok.Data;

@Data
public class AlarmStatisticsDto {
    private String type;
    private Long count;
    private String level;
}