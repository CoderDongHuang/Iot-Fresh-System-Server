package com.iot.fresh.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
public class EmailLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "alarm_id")
    private Long alarmId;
    
    @Column(name = "email_address", nullable = false)
    private String emailAddress;
    
    @Column(name = "email_subject", nullable = false)
    private String emailSubject;
    
    @Column(name = "email_content", columnDefinition = "TEXT", nullable = false)
    private String emailContent;
    
    @Column(name = "template_type")
    private String templateType; // high, medium, low
    
    @Column(name = "send_status", nullable = false)
    private String sendStatus; // success, failed
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}