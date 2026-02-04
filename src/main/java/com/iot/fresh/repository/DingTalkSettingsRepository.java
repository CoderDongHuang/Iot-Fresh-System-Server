package com.iot.fresh.repository;

import com.iot.fresh.entity.DingTalkSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DingTalkSettingsRepository extends JpaRepository<DingTalkSettings, Long> {
    DingTalkSettings findByUserId(Long userId);
}