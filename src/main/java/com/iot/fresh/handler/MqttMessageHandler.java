package com.iot.fresh.handler;

import com.iot.fresh.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

@Component
public class MqttMessageHandler {

    @Autowired
    private DataService dataService;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(@Payload String payload, @Headers MessageHeaders headers) {
        // 获取主题信息
        String topic = (String) headers.get("mqtt_receivedTopic");
        
        if (topic != null) {
            // 提取设备ID (从类似 "device/V001/data" 的主题中)
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 3) {
                String vid = topicParts[1]; // 设备ID
                String messageType = topicParts[2]; // 消息类型 (data, alarm, status, rfid)

                switch (messageType) {
                    case "data":
                        // 处理设备数据
                        dataService.processDeviceDataFromMqtt(vid, payload);
                        break;
                    case "alarm":
                        // 处理报警数据
                        dataService.processDeviceDataFromMqtt(vid, payload);
                        break;
                    case "status":
                        // 处理状态数据
                        dataService.processDeviceDataFromMqtt(vid, payload);
                        break;
                    case "rfid":
                        // 处理RFID数据
                        dataService.processDeviceDataFromMqtt(vid, payload);
                        break;
                    default:
                        System.out.println("Unknown message type: " + messageType);
                        break;
                }
            }
        }
    }
}