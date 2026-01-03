package com.iot.fresh.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_data")
@Data
@EntityListeners(AuditingEntityListener.class)
public class DeviceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vid", nullable = false)
    private String vid; // 设备唯一标识符

    @Column(name = "device_name")
    private String deviceName; // 设备名称

    @Column(name = "device_type")
    private String deviceType;

    // 设备数据字段，根据文档中的MQTT主题设计
    @Column(name = "tin")
    private Double tin; // 内部温度
    @Column(name = "tout")
    private Double tout; // 外部温度
    @Column(name = "hin")
    private Double hin; // 内部湿度
    @Column(name = "hout")
    private Double hout; // 外部湿度
    @Column(name = "lxin")
    private Integer lxin; // 内部光照
    @Column(name = "light")
    private Integer light; // 光照强度
    @Column(name = "pid")
    private String pid; // 产品ID
    @Column(name = "vstatus")
    private Integer vstatus; // 设备状态
    @Column(name = "battery")
    private Integer battery; // 电池电量
    @Column(name = "brightness")
    private Integer brightness; // 亮度
    @Column(name = "speed_m1")
    private Integer speedM1; // 风机1速度
    @Column(name = "speed_m2")
    private Integer speedM2; // 风机2速度

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "timestamp")
    private LocalDateTime timestamp; // 数据时间戳
}