package com.iot.fresh.dto;

import lombok.Data;

@Data
public class DeviceCurrentDataDto {
    private Double tin;
    private Double tout;
    private Integer hin;
    private Integer hout;
    private Integer lxin;
    private Integer lxout;
    private Integer brightness;
    private Integer vStatus;
}