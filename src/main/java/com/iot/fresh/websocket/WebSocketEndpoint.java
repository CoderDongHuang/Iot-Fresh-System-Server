package com.iot.fresh.websocket;

import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws")
@Component
public class WebSocketEndpoint {

    // 存储所有连接的会话
    private static CopyOnWriteArraySet<WebSocketEndpoint> webSockets = new CopyOnWriteArraySet<>();
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSockets.add(this);
        System.out.println("WebSocket连接已建立，当前连接数: " + webSockets.size());
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("收到消息: " + message);
        // 广播消息给所有连接的客户端
        for (WebSocketEndpoint webSocket : webSockets) {
            try {
                webSocket.session.getBasicRemote().sendText("服务器收到消息: " + message);
            } catch (IOException e) {
                e.printStackTrace();
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
}