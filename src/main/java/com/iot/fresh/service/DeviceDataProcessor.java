package com.iot.fresh.service;

import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.dto.AlarmDto;
import com.iot.fresh.websocket.DeviceWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceDataProcessor {

    @Autowired
    private DataService dataService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AlarmService alarmService;

    /**
     * 处理从设备接收到的数据
     */
    public void processDeviceData(String vid, DeviceDataDto deviceDataDto) {
        if (deviceDataDto != null) {
            // 更新设备心跳
            deviceService.updateDeviceHeartbeat(vid);

            // 保存设备数据
            dataService.saveDeviceData(deviceDataDto);

            // 检查是否需要触发报警
            checkForAlarms(vid, deviceDataDto);

            // 可以在这里添加其他业务逻辑
            System.out.println("处理设备 " + vid + " 的数据: " + deviceDataDto);
        } else {
            // 只更新设备心跳
            deviceService.updateDeviceHeartbeat(vid);
        }
    }

    /**
     * 检查是否需要触发报警
     */
    private void checkForAlarms(String vid, DeviceDataDto deviceDataDto) {
        // 这里可以实现具体的报警逻辑
        // 例如：温度过高、湿度过低等
        if (deviceDataDto.getTin() != null) {
            if (deviceDataDto.getTin() > 30.0) {
                // 温度过高报警
                System.out.println("设备 " + vid + " 温度过高: " + deviceDataDto.getTin());
                // 创建报警
                createAlarm(vid, "temperature", "high", "温度过高: " + deviceDataDto.getTin() + "°C");
            } else if (deviceDataDto.getTin() < 0.0) {
                // 温度过低报警
                System.out.println("设备 " + vid + " 温度过低: " + deviceDataDto.getTin());
                // 创建报警
                createAlarm(vid, "temperature", "high", "温度过低: " + deviceDataDto.getTin() + "°C");
            }
        }

        if (deviceDataDto.getBattery() != null && deviceDataDto.getBattery() < 20) {
            // 电池电量低报警
            System.out.println("设备 " + vid + " 电池电量低: " + deviceDataDto.getBattery() + "%");
            createAlarm(vid, "battery", "medium", "电池电量低: " + deviceDataDto.getBattery() + "%");
        }
    }

    /**
     * 创建报警
     */
    private void createAlarm(String vid, String alarmType, String alarmLevel, String message) {
        // 这里应该根据vid获取设备ID，然后创建报警
        // 为简化实现，我们直接创建一个报警对象
        AlarmDto alarmDto = new AlarmDto();
        // 实际实现中需要根据vid获取设备ID
        alarmDto.setDeviceName(vid);
        alarmDto.setAlarmType(alarmType);
        alarmDto.setAlarmLevel(alarmLevel);
        alarmDto.setMessage(message);
        alarmDto.setStatus("active");
        
        // 保存报警
        alarmService.createAlarm(alarmDto);
    }

    /**
     * 发送控制命令到设备
     */
    public void sendControlCommand(String vid, String command) {
        try {
            DeviceWebSocket.sendControlCommand(vid, command);
        } catch (Exception e) {
            System.err.println("发送控制命令失败: " + e.getMessage());
        }
    }
}