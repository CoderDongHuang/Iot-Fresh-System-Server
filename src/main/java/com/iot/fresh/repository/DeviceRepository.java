package com.iot.fresh.repository;

import com.iot.fresh.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    /**
     * 根据设备VID查找设备
     */
    Optional<Device> findByVid(String vid);
    
    /**
     * 检查设备VID是否存在
     */
    boolean existsByVid(String vid);
    
    /**
     * 根据设备状态查找设备列表
     */
    List<Device> findByStatus(Integer status);
    
    /**
     * 根据设备状态统计设备数量
     */
    long countByStatus(Integer status);
    
    /**
     * 根据设备名称或VID模糊查询并分页
     */
    Page<Device> findByDeviceNameContainingOrVidContaining(String deviceName, String vid, Pageable pageable);
    
    /**
     * 根据设备名称或VID模糊查询并分页，带状态过滤
     */
    Page<Device> findByDeviceNameContainingOrVidContainingAndStatus(String deviceName, String vid, Integer status, Pageable pageable);
    
    /**
     * 根据设备状态分页查询
     */
    Page<Device> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据设备类型查找设备列表
     */
    List<Device> findByDeviceType(String deviceType);
    
    /**
     * 根据设备名称模糊查询
     */
    @Query("SELECT d FROM Device d WHERE d.deviceName LIKE %:deviceName%")
    List<Device> findByDeviceNameContaining(@Param("deviceName") String deviceName);
    
    /**
     * 统计在线设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 1")
    Long countOnlineDevices();
    
    /**
     * 统计离线设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 0")
    Long countOfflineDevices();
}