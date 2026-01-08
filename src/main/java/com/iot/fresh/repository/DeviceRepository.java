package com.iot.fresh.repository;

import com.iot.fresh.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByVid(String vid);
    
    // 根据设备名称模糊查询
    Page<Device> findByDeviceNameContaining(String deviceName, Pageable pageable);
    
    // 根据设备VID模糊查询
    Page<Device> findByVidContaining(String vid, Pageable pageable);
    
    // 根据设备名称或VID模糊查询
    @Query("SELECT d FROM Device d WHERE d.deviceName LIKE %:keyword% OR d.vid LIKE %:keyword%")
    Page<Device> findByDeviceNameContainingOrVidContaining(String keyword, Pageable pageable);
    
    // 根据设备名称或VID模糊查询及状态
    @Query("SELECT d FROM Device d WHERE (d.deviceName LIKE %:keyword% OR d.vid LIKE %:keyword%) AND d.status = :status")
    Page<Device> findByDeviceNameContainingOrVidContainingAndStatus(String keyword, Integer status, Pageable pageable);
    
    // 根据状态查询
    Page<Device> findByStatus(Integer status, Pageable pageable);
    
    // 根据设备名称和状态查询
    Page<Device> findByDeviceNameContainingAndStatus(String deviceName, Integer status, Pageable pageable);
    
    // 根据设备状态统计数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status")
    Long countByStatus(Integer status);
}