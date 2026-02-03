package com.iot.fresh.repository;

import com.iot.fresh.entity.EmailLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailLogsRepository extends JpaRepository<EmailLogs, Long> {
}