package com.iot.fresh.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_data")
@Data
@EntityListeners(AuditingEntityListener.class)
public class DeviceData implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vid", nullable = false)
    private String vid; // 设备唯一标识符

    // 设备数据字段，根据实际的数据库表结构
    @Column(name = "tin")
    private Double tin; // 内部温度
    @Column(name = "tout")
    private Double tout; // 外部温度
    @Column(name = "hin")
    private Integer hin; // 内部湿度
    @Column(name = "hout")
    private Integer hout; // 外部湿度
    @Column(name = "lxin")
    private Integer lxin; // 内部光照
    @Column(name = "lxout")
    private Integer lxout; // 外部光照
    @Column(name = "brightness")
    private Integer brightness; // 亮度
    @Column(name = "vstatus")
    private Integer vstatus; // 设备状态

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "timestamp")
    private LocalDateTime timestamp; // 数据时间戳
}