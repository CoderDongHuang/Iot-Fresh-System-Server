package com.iot.fresh.repository;

import com.iot.fresh.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByDeviceId(Long deviceId);
    List<Alarm> findByVid(String vid);
    List<Alarm> findByDeviceName(String deviceName);
    List<Alarm> findByStatus(String status);
}