package com.iot.fresh.repository;

import com.iot.fresh.entity.SmsSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsSettingsRepository extends JpaRepository<SmsSettings, Long> {
    
    SmsSettings findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}