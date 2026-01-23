package com.iot.fresh.repository;

import com.iot.fresh.entity.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByDeviceId(Long deviceId);
    List<Alarm> findByVid(String vid);
    List<Alarm> findByDeviceName(String deviceName);
    List<Alarm> findByStatus(String status);
    
    // 按报警级别查询
    List<Alarm> findByAlarmLevel(String alarmLevel);
    Page<Alarm> findByAlarmLevel(String alarmLevel, Pageable pageable);
    
    // 按报警级别和状态查询
    Page<Alarm> findByAlarmLevelAndStatus(String alarmLevel, String status, Pageable pageable);
    
    // 按状态查询
    Page<Alarm> findByStatus(String status, Pageable pageable);
    
    // 按报警内容和设备名模糊查询
    Page<Alarm> findByMessageContainingOrDeviceNameContaining(String message, String deviceName, Pageable pageable);
    
    // 按报警内容或设备名模糊查询且状态匹配
    Page<Alarm> findByMessageContainingOrDeviceNameContainingAndStatus(String message, String deviceName, String status, Pageable pageable);
    
    // 按报警内容或设备名模糊查询且级别和状态匹配
    Page<Alarm> findByMessageContainingOrDeviceNameContainingAndAlarmLevelAndStatus(String message, String deviceName, String alarmLevel, String status, Pageable pageable);
    
    // 按报警内容、状态和设备名模糊查询
    Page<Alarm> findByMessageContainingAndStatusAndDeviceNameContaining(String message, String status, String deviceName, Pageable pageable);
    
    // 按报警内容、级别和设备名模糊查询
    Page<Alarm> findByMessageContainingAndAlarmLevelAndDeviceNameContaining(String message, String alarmLevel, String deviceName, Pageable pageable);
    
    // 按报警内容、级别、状态和设备名模糊查询
    Page<Alarm> findByMessageContainingAndAlarmLevelAndStatusAndDeviceNameContaining(String message, String alarmLevel, String status, String deviceName, Pageable pageable);
    
    // 按级别统计
    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.alarmLevel = :level")
    Long countByAlarmLevel(String level);
    
    // 按状态统计
    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.status = :status")
    Long countByStatus(String status);
    
    // 按级别和状态统计
    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.alarmLevel = :level AND a.status = :status")
    Long countByAlarmLevelAndStatus(String level, String status);
    
    // 按时间范围查询
    Page<Alarm> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end, Pageable pageable);
    
    // 按报警内容或设备名模糊查询且时间范围匹配
    Page<Alarm> findByMessageContainingOrDeviceNameContainingAndCreatedAtBetween(String message, String deviceName, java.time.LocalDateTime start, java.time.LocalDateTime end, Pageable pageable);
    
    // 按报警级别、状态和时间范围查询
    Page<Alarm> findByAlarmLevelAndStatusAndCreatedAtBetween(String alarmLevel, String status, java.time.LocalDateTime start, java.time.LocalDateTime end, Pageable pageable);
    
    // 按报警内容或设备名模糊查询且级别、状态和时间范围匹配
    Page<Alarm> findByMessageContainingOrDeviceNameContainingAndAlarmLevelAndStatusAndCreatedAtBetween(String message, String deviceName, String alarmLevel, String status, java.time.LocalDateTime start, java.time.LocalDateTime end, Pageable pageable);
}