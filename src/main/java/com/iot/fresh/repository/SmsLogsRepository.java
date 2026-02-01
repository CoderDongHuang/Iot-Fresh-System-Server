package com.iot.fresh.repository;

import com.iot.fresh.entity.SmsLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SmsLogsRepository extends JpaRepository<SmsLogs, Long> {
    
    List<SmsLogs> findByAlarmId(Long alarmId);
    
    List<SmsLogs> findByPhoneNumber(String phoneNumber);
    
    List<SmsLogs> findBySendStatus(String sendStatus);
}