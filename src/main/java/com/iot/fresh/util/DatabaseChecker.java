package com.iot.fresh.util;

import com.iot.fresh.entity.Device;
import com.iot.fresh.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseChecker implements CommandLineRunner {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 数据库设备表检查 ===");
        List<Device> devices = deviceRepository.findAll();
        System.out.println("设备总数: " + devices.size());
        
        if (devices.isEmpty()) {
            System.out.println("提示: 设备表中没有数据，您可能需要先添加一些测试数据。");
        } else {
            System.out.println("现有设备列表:");
            for (Device device : devices) {
                System.out.println("- VID: " + device.getVid() + ", Name: " + device.getDeviceName() + 
                                 ", Type: " + device.getDeviceType() + ", Status: " + device.getStatus());
            }
        }
        
        System.out.println("=== 检查完成 ===");
    }
}