package com.iot.fresh.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.service.AlarmService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws")
@Component
public class WebSocketEndpoint implements ApplicationContextAware {

    // 存储所有连接的会话
    private static CopyOnWriteArraySet<WebSocketEndpoint> webSockets = new CopyOnWriteArraySet<>();
    private Session session;
    
    private static ApplicationContext applicationContext;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        WebSocketEndpoint.applicationContext = applicationContext;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSockets.add(this);
        System.out.println("WebSocket连接已建立，当前连接数: " + webSockets.size());
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("收到消息: " + message);
        
        // 尝试解析消息类型
        if (message.equals("get_alarm_statistics")) {
            sendAlarmStatistics();
        } else {
            // 广播消息给所有连接的客户端
            for (WebSocketEndpoint webSocket : webSockets) {
                try {
                    webSocket.session.getBasicRemote().sendText("服务器收到消息: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnClose
    public void onClose() {
        webSockets.remove(this);
        System.out.println("WebSocket连接已关闭，当前连接数: " + webSockets.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket发生错误: " + error.getMessage());
        error.printStackTrace();
    }

    // 发送消息给所有客户端
    public static void sendMessageToAll(String message) {
        for (WebSocketEndpoint webSocket : webSockets) {
            try {
                webSocket.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 发送报警统计数据
    private void sendAlarmStatistics() {
        try {
            // 获取报警统计服务
            AlarmService alarmService = applicationContext.getBean(AlarmService.class);
            var statsResponse = alarmService.getAlarmStatistics();
            
            // 构建返回消息
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", statsResponse.getData());
            response.put("type", "alarm_statistics_response");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // 发送回所有客户端
            String jsonResponse = objectMapper.writeValueAsString(response);
            for (WebSocketEndpoint webSocket : webSockets) {
                try {
                    webSocket.session.getBasicRemote().sendText(jsonResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("发送报警统计数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
