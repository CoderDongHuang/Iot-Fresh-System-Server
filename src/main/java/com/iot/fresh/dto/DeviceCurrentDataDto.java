package com.iot.fresh.dto;

import lombok.Data;

@Data
public class DeviceCurrentDataDto {
    private Double tin;
    private Double tout;
    private Integer lxin;
    private Integer vStatus;
    private Integer battery;
    private Integer brightness;
    private Integer speedM1;
    private Integer speedM2;
    private Double hin;
    private Double hout;
}