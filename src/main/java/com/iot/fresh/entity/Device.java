package com.iot.fresh.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vid", unique = true, nullable = false)
    private String vid; // 设备唯一标识符

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_type")
    private String deviceType;

    // 0:离线, 1:在线, 2:故障, 3:维护
    private Integer status = 0;

    private String location;

    @Column(name = "contact_phone")
    private String contactPhone;

    private String description;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}