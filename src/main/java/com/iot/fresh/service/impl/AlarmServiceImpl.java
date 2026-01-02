package com.iot.fresh.service.impl;

import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.Alarm;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.service.AlarmService;
import com.iot.fresh.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private DeviceService deviceService;

    @Override
    public ApiResponse<List<AlarmDto>> getAllAlarms() {
        List<Alarm> alarms = alarmRepository.findAll();
        List<AlarmDto> alarmDtos = alarms.stream().map(this::convertToAlarmDto).collect(Collectors.toList());
        return ApiResponse.success(alarmDtos);
    }

    @Override
    public ApiResponse<List<AlarmDto>> getAlarmsByDeviceId(Long deviceId) {
        List<Alarm> alarms = alarmRepository.findByDeviceId(deviceId);
        List<AlarmDto> alarmDtos = alarms.stream().map(this::convertToAlarmDto).collect(Collectors.toList());
        return ApiResponse.success(alarmDtos);
    }

    @Override
    public ApiResponse<List<AlarmDto>> getAlarmsByStatus(String status) {
        List<Alarm> alarms = alarmRepository.findByStatus(status);
        List<AlarmDto> alarmDtos = alarms.stream().map(this::convertToAlarmDto).collect(Collectors.toList());
        return ApiResponse.success(alarmDtos);
    }

    @Override
    public ApiResponse<AlarmDto> createAlarm(AlarmDto alarmDto) {
        Alarm alarm = new Alarm();
        alarm.setDeviceId(alarmDto.getDeviceId());
        alarm.setDeviceName(alarmDto.getDeviceName());
        alarm.setAlarmType(alarmDto.getAlarmType());
        alarm.setAlarmLevel(alarmDto.getAlarmLevel());
        alarm.setMessage(alarmDto.getMessage());
        alarm.setStatus(alarmDto.getStatus() != null ? alarmDto.getStatus() : "active");
        alarm.setCreatedAt(LocalDateTime.now());

        Alarm savedAlarm = alarmRepository.save(alarm);
        AlarmDto savedAlarmDto = convertToAlarmDto(savedAlarm);
        return ApiResponse.success("报警创建成功", savedAlarmDto);
    }

    @Override
    public ApiResponse<AlarmDto> resolveAlarm(Long id) {
        Optional<Alarm> alarmOpt = alarmRepository.findById(id);
        if (alarmOpt.isPresent()) {
            Alarm alarm = alarmOpt.get();
            alarm.setStatus("resolved");
            alarm.setResolvedAt(LocalDateTime.now());
            
            Alarm updatedAlarm = alarmRepository.save(alarm);
            AlarmDto updatedAlarmDto = convertToAlarmDto(updatedAlarm);
            return ApiResponse.success("报警已解决", updatedAlarmDto);
        }
        return ApiResponse.error("报警不存在");
    }

    private AlarmDto convertToAlarmDto(Alarm alarm) {
        AlarmDto dto = new AlarmDto();
        dto.setId(alarm.getId());
        dto.setDeviceId(alarm.getDeviceId());
        dto.setDeviceName(alarm.getDeviceName());
        dto.setAlarmType(alarm.getAlarmType());
        dto.setAlarmLevel(alarm.getAlarmLevel());
        dto.setMessage(alarm.getMessage());
        dto.setStatus(alarm.getStatus());
        dto.setCreatedAt(alarm.getCreatedAt());
        dto.setResolvedAt(alarm.getResolvedAt());
        return dto;
    }
}