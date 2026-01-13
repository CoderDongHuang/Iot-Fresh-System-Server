package com.iot.fresh.config;

import com.iot.fresh.entity.Alarm;
import com.iot.fresh.entity.Device;
import com.iot.fresh.repository.AlarmRepository;
import com.iot.fresh.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有设备数据，如果没有则创建一些测试设备
        if (deviceRepository.count() == 0) {
            createTestDevices();
        }

        // 检查是否已有报警数据，如果没有则创建一些测试报警
        if (alarmRepository.count() == 0) {
            createTestAlarms();
        }
    }

    private void createTestDevices() {
        // 创建一些测试设备
        String[] deviceNames = {"温度传感器-001", "湿度传感器-002", "门禁控制器-003", "摄像头-004", "温控器-005"};
        String[] vids = {"TEMP_001", "HUMID_002", "DOOR_003", "CAMERA_004", "THERM_005"};

        for (int i = 0; i < deviceNames.length; i++) {
            Device device = new Device();
            device.setVid(vids[i]);
            device.setDeviceName(deviceNames[i]); // 使用正确的字段名
            device.setDeviceType("传感器"); // 使用正确的字段名
            device.setLocation("仓库A区");
            device.setStatus(1); // 在线状态
            device.setCreatedAt(LocalDateTime.now());
            device.setUpdatedAt(LocalDateTime.now());
            deviceRepository.save(device);
        }
    }

    private void createTestAlarms() {
        // 获取所有设备用于创建测试报警
        Iterable<Device> devices = deviceRepository.findAll();
        
        // 创建一些测试报警数据
        String[] alarmTypes = {"温度异常", "湿度异常", "设备故障", "安全警告"};
        String[] alarmLevels = {"high", "medium", "low"};
        String[] statuses = {"active", "resolved", "ignored"};
        String[] messages = {
            "温度超过阈值",
            "湿度异常升高",
            "设备连接超时",
            "安全检测异常",
            "硬件故障警告",
            "网络连接中断"
        };

        Random random = new Random();
        int alarmCount = 0;

        for (Device device : devices) {
            // 为每个设备创建几个测试报警
            for (int i = 0; i < 3; i++) {
                Alarm alarm = new Alarm();
                alarm.setDeviceId(device.getId());
                alarm.setVid(device.getVid());
                alarm.setDeviceName(device.getDeviceName()); // 使用正确的字段名
                alarm.setAlarmType(alarmTypes[random.nextInt(alarmTypes.length)]);
                alarm.setAlarmLevel(alarmLevels[random.nextInt(alarmLevels.length)]);
                alarm.setMessage(messages[random.nextInt(messages.length)] + " - 测试数据");
                alarm.setStatus(statuses[random.nextInt(statuses.length)]);
                
                // 设置创建时间和解决时间
                LocalDateTime now = LocalDateTime.now();
                alarm.setCreatedAt(now.minusHours(random.nextInt(24 * 7))); // 一周内的随机时间
                if ("resolved".equals(alarm.getStatus()) || "ignored".equals(alarm.getStatus())) {
                    alarm.setResolvedAt(now.minusHours(random.nextInt(24 * 7)));
                }
                alarm.setUpdatedAt(now);

                alarmRepository.save(alarm);
                alarmCount++;
                
                if (alarmCount >= 20) break; // 限制总数量
            }
            if (alarmCount >= 20) break; // 限制总数量
        }
    }
}