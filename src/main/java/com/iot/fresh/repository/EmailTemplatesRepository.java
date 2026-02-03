package com.iot.fresh.repository;

import com.iot.fresh.entity.EmailTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplatesRepository extends JpaRepository<EmailTemplates, Long> {
    
    EmailTemplates findByTemplateType(String templateType);
}