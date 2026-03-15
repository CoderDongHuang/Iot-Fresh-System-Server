package com.iot.fresh.service;

import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.entity.DeviceDataHistory;
import com.iot.fresh.repository.DeviceDataHistoryRepository;
import com.iot.fresh.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 设备历史数据服务
 * 新增功能：处理历史数据表的插入逻辑
 * 不修改昨天已实现的代码
 */
@Service
public class DeviceDataHistoryService {
    
    @Autowired
    private DeviceDataHistoryRepository deviceDataHistoryRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * 处理数据主题的历史数据插入
     * 每次都要创建新记录，状态和设备表保持一致
     */
    @Transactional
    public void saveDeviceDataHistory(DeviceDataDto deviceDataDto) {
        try {
            System.out.println("=== Starting device data history save ===");
            System.out.println("DeviceDataDto - VID: " + deviceDataDto.getVid() + ", Tin: " + deviceDataDto.getTin());
            
            // 获取设备表的状态作为默认值
            Integer deviceStatus = getDeviceStatusFromDeviceTable(deviceDataDto.getVid());
            System.out.println("Device table status: " + deviceStatus);
            
            // 创建历史数据记录
            DeviceDataHistory history = new DeviceDataHistory();
            history.setVid(deviceDataDto.getVid());
            history.setTin(deviceDataDto.getTin());
            history.setTout(deviceDataDto.getTout());
            history.setHin(deviceDataDto.getHin());
            history.setHout(deviceDataDto.getHout());
            history.setLxin(deviceDataDto.getLxin());
            history.setLxout(deviceDataDto.getLxout());
            history.setBrightness(deviceDataDto.getBrightness());
            history.setVstatus(deviceStatus); // 状态和设备表保持一致
            history.setUpdatedAt(LocalDateTime.now()); // 当前时间
            
            // 保存历史数据
            DeviceDataHistory savedHistory = deviceDataHistoryRepository.save(history);
            
            System.out.println("Saved device data history - ID: " + savedHistory.getId() + ", VID: " + savedHistory.getVid());
            System.out.println("History data - Tin: " + savedHistory.getTin() + ", VStatus: " + savedHistory.getVstatus());
            System.out.println("History updated_at: " + savedHistory.getUpdatedAt());
            
            // 强制刷新到数据库
            deviceDataHistoryRepository.flush();
            System.out.println("Flushed device data history to database");
            
            System.out.println("=== Device data history save COMPLETED ===");
            
        } catch (Exception e) {
            System.err.println("ERROR saving device data history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理状态主题的历史数据插入
     * 创建新记录，数据和上一条保持一致，状态更改
     */
    @Transactional
    public void saveStatusHistory(String vid, Integer newStatus) {
        try {
            System.out.println("=== Starting status history save ===");
            System.out.println("Status history - VID: " + vid + ", new status: " + newStatus);
            
            // 查找最新的历史数据记录
            Optional<DeviceDataHistory> latestHistoryOpt = deviceDataHistoryRepository.findTopByVidOrderByUpdatedAtDesc(vid);
            
            DeviceDataHistory history = new DeviceDataHistory();
            history.setVid(vid);
            history.setVstatus(newStatus); // 使用新的状态
            history.setUpdatedAt(LocalDateTime.now()); // 当前时间
            
            if (latestHistoryOpt.isPresent()) {
                // 有历史记录，数据和上一条保持一致
                DeviceDataHistory latestHistory = latestHistoryOpt.get();
                history.setTin(latestHistory.getTin());
                history.setTout(latestHistory.getTout());
                history.setHin(latestHistory.getHin());
                history.setHout(latestHistory.getHout());
                history.setLxin(latestHistory.getLxin());
                history.setLxout(latestHistory.getLxout());
                history.setBrightness(latestHistory.getBrightness());
                
                System.out.println("Using previous data - Tin: " + latestHistory.getTin() + ", Tout: " + latestHistory.getTout());
                System.out.println("Status changed from " + latestHistory.getVstatus() + " to " + newStatus);
            } else {
                // 没有历史记录，使用默认值
                System.out.println("No previous history found, using default values");
                // 传感器数据可以为null，状态已经设置
            }
            
            // 保存历史数据
            DeviceDataHistory savedHistory = deviceDataHistoryRepository.save(history);
            
            System.out.println("Saved status history - ID: " + savedHistory.getId() + ", VID: " + savedHistory.getVid());
            System.out.println("Status history - VStatus: " + savedHistory.getVstatus() + ", updated_at: " + savedHistory.getUpdatedAt());
            
            // 强制刷新到数据库
            deviceDataHistoryRepository.flush();
            System.out.println("Flushed status history to database");
            
            System.out.println("=== Status history save COMPLETED ===");
            
        } catch (Exception e) {
            System.err.println("ERROR saving status history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从设备表获取对应设备的状态
     */
    private Integer getDeviceStatusFromDeviceTable(String vid) {
        try {
            Optional<com.iot.fresh.entity.Device> deviceOpt = deviceRepository.findByVid(vid);
            if (deviceOpt.isPresent()) {
                com.iot.fresh.entity.Device device = deviceOpt.get();
                Integer status = device.getStatus();
                System.out.println("Found device in devices table - VID: " + vid + ", status: " + status);
                return status;
            } else {
                System.out.println("Device not found in devices table for VID: " + vid + ", using default status: 1");
                return 1; // 默认在线状态
            }
        } catch (Exception e) {
            System.err.println("Error getting device status from device table: " + e.getMessage());
            return 1; // 默认在线状态
        }
    }
    
    /**
     * 根据设备ID和时间范围分页查询历史数据
     */
    public Page<DeviceDataHistory> getHistoryDataByVidAndTimeRange(String vid, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            System.out.println("Querying history data by VID and time range - VID: " + vid + ", Start: " + startTime + ", End: " + endTime);
            
            Page<DeviceDataHistory> result = deviceDataHistoryRepository.findByVidAndUpdatedAtBetween(vid, startTime, endTime, pageable);
            
            System.out.println("Found " + result.getTotalElements() + " history records for VID: " + vid);
            return result;
            
        } catch (Exception e) {
            System.err.println("Error querying history data by VID and time range: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 根据时间范围分页查询所有设备的历史数据
     */
    public Page<DeviceDataHistory> getHistoryDataByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            System.out.println("Querying history data by time range - Start: " + startTime + ", End: " + endTime);
            
            Page<DeviceDataHistory> result = deviceDataHistoryRepository.findByUpdatedAtBetween(startTime, endTime, pageable);
            
            System.out.println("Found " + result.getTotalElements() + " history records in time range");
            return result;
            
        } catch (Exception e) {
            System.err.println("Error querying history data by time range: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}