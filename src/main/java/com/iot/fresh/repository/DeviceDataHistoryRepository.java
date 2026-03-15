package com.iot.fresh.repository;

import com.iot.fresh.entity.DeviceDataHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceDataHistoryRepository extends JpaRepository<DeviceDataHistory, Long> {
    
    /**
     * 根据设备ID查找最新的历史数据记录
     */
    @Query("SELECT d FROM DeviceDataHistory d WHERE d.vid = :vid ORDER BY d.updatedAt DESC")
    Optional<DeviceDataHistory> findTopByVidOrderByUpdatedAtDesc(@Param("vid") String vid);
    
    /**
     * 根据设备ID和时间范围查找历史数据
     */
    List<DeviceDataHistory> findByVidAndUpdatedAtBetween(String vid, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据设备ID查找所有历史数据
     */
    List<DeviceDataHistory> findByVidOrderByUpdatedAtDesc(String vid);
    
    /**
     * 根据时间范围查找所有设备的历史数据
     */
    List<DeviceDataHistory> findByUpdatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据设备ID和时间范围分页查询历史数据
     */
    Page<DeviceDataHistory> findByVidAndUpdatedAtBetween(String vid, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据时间范围分页查询所有设备的历史数据
     */
    Page<DeviceDataHistory> findByUpdatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}