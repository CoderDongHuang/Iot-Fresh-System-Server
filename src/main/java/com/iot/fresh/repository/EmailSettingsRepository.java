package com.iot.fresh.repository;

import com.iot.fresh.entity.EmailSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailSettingsRepository extends JpaRepository<EmailSettings, Long> {
    
    EmailSettings findByUserId(Long userId);
}