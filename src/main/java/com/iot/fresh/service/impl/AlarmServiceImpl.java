package com.iot.fresh.service.impl;

import com.iot.fresh.dto.AlarmDataDto;
import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.Alarm;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;

    @Override
    public ApiResponse<String> processAlarm(AlarmDataDto alarmDataDto) {
        Alarm alarm = new Alarm();
        alarm.setVid(alarmDataDto.getVid());
        alarm.setDeviceName(alarmDataDto.getVid()); // 使用VID作为设备名称
        alarm.setAlarmType(alarmDataDto.getAlarmType());
        
        // 如果消息为空，使用code构建消息
        if (alarmDataDto.getMessage() != null && !alarmDataDto.getMessage().isEmpty()) {
            alarm.setMessage(alarmDataDto.getMessage());
        } else if (alarmDataDto.getCode() != null) {
            alarm.setMessage("报警代码: " + alarmDataDto.getCode());
        } else {
            alarm.setMessage("设备报警");
        }
        
        alarm.setTimestamp(alarmDataDto.getTimestamp() != null ? alarmDataDto.getTimestamp() : LocalDateTime.now());
        alarm.setStatus("active"); // 默认状态为活跃

        alarmRepository.save(alarm);
        return ApiResponse.success("报警数据保存成功", alarm.getId().toString());
    }

    @Override
    public ApiResponse<List<AlarmDto>> getAlarmsByVid(String vid) {
        // 实现获取指定设备的报警列表
        List<Alarm> alarms = alarmRepository.findByVid(vid);
        List<AlarmDto> result = new ArrayList<>();
        for (Alarm alarm : alarms) {
            result.add(convertToDto(alarm));
        }
        return ApiResponse.success("获取成功", result);
    }

    @Override
    public ApiResponse<String> handleAlarm(Long alarmId) {
        // 实现处理报警
        Alarm alarm = alarmRepository.findById(alarmId).orElse(null);
        if (alarm != null) {
            alarm.setStatus("handled");
            alarm.setHandledAt(LocalDateTime.now());
            alarmRepository.save(alarm);
            return ApiResponse.success("报警已处理");
        }
        return ApiResponse.error("报警不存在");
    }

    @Override
    public ApiResponse<List<AlarmDto>> getAlarmsByDeviceId(Long deviceId) {
        List<Alarm> alarms = alarmRepository.findByDeviceId(deviceId);
        List<AlarmDto> result = new ArrayList<>();
        for (Alarm alarm : alarms) {
            result.add(convertToDto(alarm));
        }
        return ApiResponse.success("获取成功", result);
    }

    @Override
    public ApiResponse<List<AlarmDto>> getAlarmsByStatus(String status) {
        List<Alarm> alarms = alarmRepository.findByStatus(status);
        List<AlarmDto> result = new ArrayList<>();
        for (Alarm alarm : alarms) {
            result.add(convertToDto(alarm));
        }
        return ApiResponse.success("获取成功", result);
    }

    @Override
    public ApiResponse<List<AlarmDto>> getAllAlarms() {
        List<Alarm> alarms = alarmRepository.findAll();
        List<AlarmDto> result = new ArrayList<>();
        for (Alarm alarm : alarms) {
            result.add(convertToDto(alarm));
        }
        return ApiResponse.success("获取成功", result);
    }

    @Override
    public ApiResponse<AlarmDto> resolveAlarm(Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId).orElse(null);
        if (alarm != null) {
            alarm.setStatus("resolved");
            alarm.setHandledAt(LocalDateTime.now());
            alarmRepository.save(alarm);
            return ApiResponse.success("报警已解决", convertToDto(alarm));
        }
        return ApiResponse.error("报警不存在");
    }

    @Override
    public ApiResponse<String> createAlarm(AlarmDto alarmDto) {
        Alarm alarm = new Alarm();
        alarm.setVid(alarmDto.getVid());
        alarm.setDeviceName(alarmDto.getDeviceName());
        alarm.setAlarmType(alarmDto.getAlarmType());
        alarm.setAlarmLevel(alarmDto.getAlarmLevel());
        alarm.setMessage(alarmDto.getMessage());
        alarm.setStatus(alarmDto.getStatus());
        alarm.setTimestamp(alarmDto.getTimestamp() != null ? alarmDto.getTimestamp() : LocalDateTime.now());

        alarmRepository.save(alarm);
        return ApiResponse.success("报警创建成功", alarm.getId().toString());
    }
    
    private AlarmDto convertToDto(Alarm alarm) {
        AlarmDto dto = new AlarmDto();
        dto.setDeviceId(alarm.getDeviceId());
        dto.setVid(alarm.getVid());
        dto.setDeviceName(alarm.getDeviceName());
        dto.setAlarmType(alarm.getAlarmType());
        dto.setAlarmLevel(alarm.getAlarmLevel());
        dto.setMessage(alarm.getMessage());
        dto.setStatus(alarm.getStatus());
        dto.setTimestamp(alarm.getTimestamp());
        return dto;
    }
}