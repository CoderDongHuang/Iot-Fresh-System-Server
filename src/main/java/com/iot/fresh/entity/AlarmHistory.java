package com.iot.fresh.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_history")
@Data
@EntityListeners(AuditingEntityListener.class)
public class AlarmHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alarm_id")
    private Long alarmId;

    @Column(name = "action")
    private String action; // 创建报警/处理/关闭

    @Column(name = "operator")
    private String operator; // 操作人

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark; // 处理备注

    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // 关联报警实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alarm_id", insertable = false, updatable = false)
    private Alarm alarm;
}