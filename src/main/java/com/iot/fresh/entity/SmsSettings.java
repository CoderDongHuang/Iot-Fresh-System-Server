package com.iot.fresh.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_settings")
@Data
public class SmsSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "enabled")
    private Boolean enabled = false;
    
    @Column(name = "phone_numbers", columnDefinition = "JSON")
    private String phoneNumbers; // JSON格式: ["13800138000", "13900139000"]
    
    @Column(name = "notify_levels", columnDefinition = "JSON")
    private String notifyLevels; // JSON格式: ["high", "medium"]
    
    @Column(name = "quiet_hours", columnDefinition = "JSON")
    private String quietHours; // JSON格式: ["22:00", "07:00"]
    
    @Column(name = "push_frequency")
    private String pushFrequency = "immediate";
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}