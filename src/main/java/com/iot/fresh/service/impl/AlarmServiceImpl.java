package com.iot.fresh.service.impl;

import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.PaginatedResponse;
import com.iot.fresh.entity.Alarm;
import com.iot.fresh.entity.Device;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.repository.DeviceRepository;
import com.iot.fresh.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public ApiResponse<PaginatedResponse<Map<String, Object>>> getAlarmList(Integer pageNum, Integer pageSize, String level, String status, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Alarm> alarmPage;
        
        if (keyword != null && !keyword.isEmpty()) {
            // 按关键词搜索（设备名称或报警内容）
            if (level != null && !level.isEmpty() && status != null && !status.isEmpty()) {
                alarmPage = alarmRepository.findByMessageContainingAndAlarmLevelAndStatusAndDeviceNameContaining(keyword, level, status, keyword, pageable);
            } else if (level != null && !level.isEmpty()) {
                alarmPage = alarmRepository.findByMessageContainingAndAlarmLevelAndDeviceNameContaining(keyword, level, keyword, pageable);
            } else if (status != null && !status.isEmpty()) {
                alarmPage = alarmRepository.findByMessageContainingAndStatusAndDeviceNameContaining(keyword, status, keyword, pageable);
            } else {
                alarmPage = alarmRepository.findByMessageContainingOrDeviceNameContaining(keyword, keyword, pageable);
            }
        } else {
            // 不按关键词搜索
            if (level != null && !level.isEmpty() && status != null && !status.isEmpty()) {
                alarmPage = alarmRepository.findByAlarmLevelAndStatus(level, status, pageable);
            } else if (level != null && !level.isEmpty()) {
                alarmPage = alarmRepository.findByAlarmLevel(level, pageable);
            } else if (status != null && !status.isEmpty()) {
                alarmPage = alarmRepository.findByStatus(status, pageable);
            } else {
                alarmPage = alarmRepository.findAll(pageable);
            }
        }
        
        List<Map<String, Object>> alarmList = alarmPage.getContent().stream().map(this::convertToAlarmMap).toList();
        
        PaginatedResponse<Map<String, Object>> paginatedResponse = new PaginatedResponse<>(
                alarmList,
                alarmPage.getTotalElements(),
                pageNum,
                pageSize
        );
        
        return ApiResponse.success(paginatedResponse);
    }

    @Override
    public ApiResponse<String> resolveAlarm(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        Alarm alarm = alarmOpt.get();
        alarm.setStatus("resolved"); // 设置为已处理
        alarm.setUpdatedAt(LocalDateTime.now());
        alarmRepository.save(alarm);
        
        return ApiResponse.success("报警已处理");
    }

    @Override
    public ApiResponse<String> ignoreAlarm(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        Alarm alarm = alarmOpt.get();
        alarm.setStatus("ignored"); // 设置为已忽略
        alarm.setUpdatedAt(LocalDateTime.now());
        alarmRepository.save(alarm);
        
        return ApiResponse.success("报警已忽略");
    }

    @Override
    public ApiResponse<String> clearAllAlarms() {
        alarmRepository.deleteAll(); // 清除所有报警
        return ApiResponse.success("所有报警已清除");
    }

    @Override
    public ApiResponse<Map<String, Object>> getAlarmDetail(Long alarmId) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(alarmId);
        if (!alarmOpt.isPresent()) {
            return ApiResponse.error("报警不存在");
        }
        
        Alarm alarm = alarmOpt.get();
        Map<String, Object> alarmDetail = convertToAlarmDetailMap(alarm);
        
        return ApiResponse.success(alarmDetail);
    }

    @Override
    public ApiResponse<Map<String, Object>> getAlarmStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总报警数
        long total = alarmRepository.count();
        statistics.put("total", total);
        
        // 按状态统计
        long pending = alarmRepository.countByStatus("pending");
        long resolved = alarmRepository.countByStatus("resolved");
        long ignored = alarmRepository.countByStatus("ignored");
        
        statistics.put("pending", pending);
        statistics.put("resolved", resolved);
        statistics.put("ignored", ignored);
        
        // 按级别统计
        long critical = alarmRepository.countByAlarmLevel("critical");
        long high = alarmRepository.countByAlarmLevel("high");
        long medium = alarmRepository.countByAlarmLevel("medium");
        long low = alarmRepository.countByAlarmLevel("low");
        
        statistics.put("critical", critical);
        statistics.put("high", high);
        statistics.put("medium", medium);
        statistics.put("low", low);
        
        return ApiResponse.success(statistics);
    }
    
    private Map<String, Object> convertToAlarmMap(Alarm alarm) {
        Map<String, Object> alarmMap = new HashMap<>();
        alarmMap.put("id", alarm.getId());
        alarmMap.put("deviceName", alarm.getDeviceName());
        alarmMap.put("alarmContent", alarm.getMessage()); // 使用message字段作为报警内容
        alarmMap.put("level", alarm.getAlarmLevel());
        alarmMap.put("status", alarm.getStatus());
        alarmMap.put("timestamp", alarm.getCreatedAt().toString()); // 使用ISO 8601格式
        alarmMap.put("extraInfo", alarm.getMessage()); // 如果没有专门的extraInfo字段，使用message
        
        return alarmMap;
    }
    
    private Map<String, Object> convertToAlarmDetailMap(Alarm alarm) {
        Map<String, Object> alarmDetail = new HashMap<>();
        alarmDetail.put("id", alarm.getId());
        alarmDetail.put("deviceName", alarm.getDeviceName());
        alarmDetail.put("alarmContent", alarm.getMessage()); // 使用message字段作为报警内容
        alarmDetail.put("level", alarm.getAlarmLevel());
        alarmDetail.put("status", alarm.getStatus());
        alarmDetail.put("timestamp", alarm.getCreatedAt().toString()); // 使用ISO 8601格式
        alarmDetail.put("extraInfo", alarm.getMessage()); // 如果没有专门的extraInfo字段，使用message
        
        // 添加设备信息
        Map<String, Object> deviceInfo = new HashMap<>();
        Device device = deviceRepository.findByVid(alarm.getVid()).orElse(null);
        if (device != null) {
            deviceInfo.put("vid", device.getVid());
            deviceInfo.put("deviceType", device.getDeviceType());
        }
        alarmDetail.put("deviceInfo", deviceInfo);
        
        return alarmDetail;
    }

    @Override
    public void processAlarm(AlarmDataDto alarmData) {
        // 创建报警记录
        Alarm alarm = new Alarm();
        alarm.setVid(alarmData.getVid());
        alarm.setAlarmType(alarmData.getAlarmType());
        alarm.setAlarmLevel("medium"); // 默认级别
        alarm.setMessage(alarmData.getMessage());
        alarm.setStatus("active"); // 默认状态
        alarm.setCreatedAt(alarmData.getTimestamp() != null ? alarmData.getTimestamp() : LocalDateTime.now());
        
        // 查找设备信息
        Optional<Device> deviceOpt = deviceRepository.findByVid(alarmData.getVid());
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            alarm.setDeviceId(device.getId());
            alarm.setDeviceName(device.getDeviceName());
        }
        
        alarmRepository.save(alarm);
        System.out.println("报警已处理并保存: " + alarm.getMessage());
    }

    @Override
    public void createAlarm(AlarmDto alarmDto) {
        // 创建报警记录
        Alarm alarm = new Alarm();
        alarm.setDeviceId(alarmDto.getDeviceId());
        alarm.setVid(alarmDto.getVid());
        alarm.setDeviceName(alarmDto.getDeviceName());
        alarm.setAlarmType(alarmDto.getAlarmType());
        alarm.setAlarmLevel(alarmDto.getAlarmLevel() != null ? alarmDto.getAlarmLevel() : "medium");
        alarm.setMessage(alarmDto.getMessage());
        alarm.setStatus(alarmDto.getStatus() != null ? alarmDto.getStatus() : "active");
        alarm.setCreatedAt(alarmDto.getTimestamp() != null ? alarmDto.getTimestamp() : LocalDateTime.now());
        
        alarmRepository.save(alarm);
        System.out.println("报警已创建: " + alarm.getMessage());
    }
}