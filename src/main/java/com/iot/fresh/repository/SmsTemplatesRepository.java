package com.iot.fresh.repository;

import com.iot.fresh.entity.SmsTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsTemplatesRepository extends JpaRepository<SmsTemplates, Long> {
    
    SmsTemplates findByTemplateType(String templateType);
}