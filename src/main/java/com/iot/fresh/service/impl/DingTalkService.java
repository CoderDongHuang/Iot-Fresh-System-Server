package com.iot.fresh.service.impl;

import com.iot.fresh.entity.DingTalkSettings;
import com.iot.fresh.repository.DingTalkSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DingTalkService {
    
    @Autowired
    private DingTalkSettingsRepository dingTalkSettingsRepository;
    
    /**
     * 发送钉钉消息的核心方法
     */
    public boolean sendDingTalkMessage(String webhookUrl, String secret, String message) {
        try {
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.error("钉钉webhook地址不能为空");
                return false;
            }
            
            log.info("开始发送钉钉消息 - Webhook: {}, 消息长度: {}", 
                webhookUrl.substring(0, Math.min(webhookUrl.length(), 50)) + "...", message.length());
            
            // 生成时间戳和签名
            long timestamp = System.currentTimeMillis();
            String sign = generateSign(secret, timestamp);
            
            // 构建请求URL - 钉钉要求使用?timestamp=xxx&sign=xxx格式
            String url = webhookUrl;
            if (secret != null && !secret.isEmpty()) {
                // 检查URL是否已经有参数
                if (url.contains("?")) {
                    url += "&timestamp=" + timestamp + "&sign=" + sign;
                } else {
                    url += "?timestamp=" + timestamp + "&sign=" + sign;
                }
            }
            
            log.debug("钉钉请求URL: {}", url.substring(0, Math.min(url.length(), 100)) + "...");
            
            // 构建消息体
            Map<String, Object> msg = new HashMap<>();
            msg.put("msgtype", "text");
            
            Map<String, String> text = new HashMap<>();
            text.put("content", message);
            msg.put("text", text);
            
            log.debug("钉钉消息体: {}", msg);
            
            // 发送HTTP请求
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(msg, headers);
            
            log.info("正在调用钉钉API...");
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("钉钉消息发送成功 - 状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
            } else {
                log.error("钉钉消息发送失败 - 状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
            }
            
            return success;
        } catch (Exception e) {
            log.error("发送钉钉消息失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 生成签名 - 钉钉官方要求的签名算法
     */
    private String generateSign(String secret, long timestamp) {
        try {
            if (secret == null || secret.isEmpty()) {
                log.debug("未配置加签密钥，跳过签名生成");
                return "";
            }
            
            // 钉钉官方签名算法：timestamp + "\n" + secret
            String stringToSign = timestamp + "\n" + secret;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            
            // Base64编码，然后进行URL编码
            String base64Sign = Base64.getEncoder().encodeToString(signData);
            String sign = URLEncoder.encode(base64Sign, StandardCharsets.UTF_8.toString());
            
            log.debug("钉钉签名生成成功 - 时间戳: {}, 原始签名: {}, URL编码签名: {}", 
                timestamp, base64Sign, sign);
            return sign;
        } catch (Exception e) {
            log.error("生成钉钉签名失败: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * 发送测试消息
     */
    public boolean sendTestMessage(String webhookUrl, String secret) {
        String testMessage = "【IoT生鲜品储运系统】钉钉机器人测试消息\n\n" +
                           "时间: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                           "状态: 测试消息发送成功\n" +
                           "说明: 钉钉机器人配置正确，可以正常接收报警通知";
        
        return sendDingTalkMessage(webhookUrl, secret, testMessage);
    }
    
    /**
     * 发送报警消息
     */
    public boolean sendAlarmMessage(String webhookUrl, String secret, String deviceName, String alarmLevel, String alarmContent, String alarmTime) {
        String message = String.format(
            "【IoT生鲜品储运系统】报警通知\n\n" +
            "设备: %s\n" +
            "报警级别: %s\n" +
            "报警内容: %s\n" +
            "报警时间: %s\n\n" +
            "请及时处理！",
            deviceName, alarmLevel, alarmContent, alarmTime
        );
        
        return sendDingTalkMessage(webhookUrl, secret, message);
    }
}