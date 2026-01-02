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

    @Column(name = "device_type")
    private String deviceType;

    // 设备数据字段，根据文档中的MQTT主题设计
    private Double tin; // 内部温度
    private Double tout; // 外部温度
    private Integer lxin; // 内部光照
    private String pid; // 产品ID
    private Integer vstatus; // 设备状态
    private Integer battery; // 电池电量
    private Integer brightness; // 亮度
    private Integer speedM1; // 风机1速度
    private Integer speedM2; // 风机2速度

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}