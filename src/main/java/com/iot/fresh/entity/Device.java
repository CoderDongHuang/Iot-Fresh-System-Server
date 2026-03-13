package com.iot.fresh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vid", unique = true, nullable = false, length = 50)
    private String vid;
    
    @Column(name = "device_name", length = 100)
    private String deviceName;
    
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1-在线，0-离线
    
    @Column(name = "location", length = 200)
    private String location;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;
    
    @Column(name = "model", length = 100)
    private String model;
    
    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "mac_address", length = 17)
    private String macAddress;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_online_time")
    private LocalDateTime lastHeartbeat;
    

}