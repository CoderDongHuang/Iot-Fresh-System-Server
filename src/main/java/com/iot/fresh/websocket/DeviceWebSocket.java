package com.iot.fresh.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.DeviceDataDto;
import com.iot.fresh.service.DeviceDataProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket/device/{vid}")
@Component
public class DeviceWebSocket {

    private static DeviceDataProcessor deviceDataProcessor;
    
    @Autowired
    public void setDeviceDataProcessor(DeviceDataProcessor deviceDataProcessor) {
        DeviceWebSocket.deviceDataProcessor = deviceDataProcessor;
    }

    // 存储所有连接的会话
    private static CopyOnWriteArraySet<DeviceWebSocket> webSockets = new CopyOnWriteArraySet<>();
    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    private Session session;
    private String vid;

    @OnOpen
    public void onOpen(Session session, @PathParam("vid") String vid) {
        this.session = session;
        this.vid = vid;
        webSockets.add(this);
        sessionPool.put(vid, session);
        
        // 更新设备心跳
        if (deviceDataProcessor != null) {
            deviceDataProcessor.processDeviceData(vid, null); // 只更新心跳
        }
        
        System.out.println("设备 " + vid + " 已连接");
    }

    @OnMessage
    public void onMessage(String message, @PathParam("vid") String vid) {
        System.out.println("收到来自设备 " + vid + " 的消息: " + message);
        
        try {
            // 解析设备发送的数据
            ObjectMapper objectMapper = new ObjectMapper();
            DeviceDataDto deviceDataDto = objectMapper.readValue(message, DeviceDataDto.class);
            deviceDataDto.setVid(vid);
            
            // 使用数据处理器处理设备数据
            if (deviceDataProcessor != null) {
                deviceDataProcessor.processDeviceData(vid, deviceDataDto);
            }
            
            // 将数据广播给所有连接的客户端
            broadcastToAllClients(vid, message);
            
        } catch (Exception e) {
            e.printStackTrace();
            // 发送错误消息回设备
            try {
                sendMessage("数据格式错误: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose() {
        webSockets.remove(this);
        sessionPool.remove(this.vid);
        System.out.println("设备 " + vid + " 已断开连接");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("设备 " + vid + " 发生错误: " + error.getMessage());
        error.printStackTrace();
    }

    // 发送消息给指定设备
    public static void sendMessage(String vid, String message) throws IOException {
        Session session = sessionPool.get(vid);
        if (session != null) {
            session.getAsyncRemote().sendText(message);
        }
    }

    // 发送消息给当前会话
    public void sendMessage(String message) throws IOException {
        this.session.getAsyncRemote().sendText(message);
    }

    // 广播消息给所有连接的客户端
    public static void broadcastToAllClients(String vid, String message) {
        for (DeviceWebSocket webSocket : webSockets) {
            try {
                if (webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText("[" + vid + "] " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 广播控制指令给指定设备
    public static void sendControlCommand(String vid, String command) {
        try {
            sendMessage(vid, command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}