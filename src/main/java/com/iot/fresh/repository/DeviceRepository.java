package com.iot.fresh.repository;

import com.iot.fresh.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByVid(String vid);
    
    // 根据设备状态统计数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status")
    Long countByStatus(Integer status);
}