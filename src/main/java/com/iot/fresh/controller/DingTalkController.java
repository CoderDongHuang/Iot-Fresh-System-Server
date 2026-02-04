package com.iot.fresh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.ResponseData;
import com.iot.fresh.entity.DingTalkSettings;
import com.iot.fresh.repository.DingTalkSettingsRepository;
import com.iot.fresh.service.impl.DingTalkService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dingtalk")
@Slf4j
public class DingTalkController {
    
    @Autowired
    private DingTalkSettingsRepository dingTalkSettingsRepository;
    
    @Autowired
    private DingTalkService dingTalkService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 获取钉钉设置
     */
    @GetMapping("/settings")
    public ResponseEntity<ResponseData<Map<String, Object>>> getDingTalkSettings() {
        try {
            DingTalkSettings settings = dingTalkSettingsRepository.findByUserId(1L);
            if (settings == null) {
                // 创建默认设置
                settings = createDefaultSettings();
                dingTalkSettingsRepository.save(settings);
            }
            
            // 转换为前端期望的格式
            Map<String, Object> result = new HashMap<>();
            result.put("id", settings.getId());
            result.put("userId", settings.getUserId());
            result.put("enabled", settings.getEnabled());
            result.put("webhookUrl", settings.getWebhookUrl());
            result.put("secret", settings.getSecret());
            
            // 将JSON字符串转换为数组
            if (settings.getNotifyLevels() != null) {
                result.put("notifyLevels", objectMapper.readValue(settings.getNotifyLevels(), List.class));
            } else {
                result.put("notifyLevels", new ArrayList<>());
            }
            
            if (settings.getQuietHours() != null) {
                result.put("quietHours", objectMapper.readValue(settings.getQuietHours(), List.class));
            } else {
                result.put("quietHours", new ArrayList<>());
            }
            
            result.put("createdAt", settings.getCreatedAt() != null ? settings.getCreatedAt().toString() : null);
            result.put("updatedAt", settings.getUpdatedAt() != null ? settings.getUpdatedAt().toString() : null);
            
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.success(result));
        } catch (Exception e) {
            log.error("获取钉钉设置失败: {}", e.getMessage());
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.error("获取钉钉设置失败"));
        }
    }
    
    /**
     * 保存钉钉设置
     */
    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<Void>> saveDingTalkSettings(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("收到保存钉钉设置请求: {}", requestData);
            
            DingTalkSettings settings = dingTalkSettingsRepository.findByUserId(1L);
            if (settings == null) {
                settings = new DingTalkSettings();
                settings.setUserId(1L);
            }
            
            // 处理前端发送的数组数据，转换为JSON字符串
            settings.setEnabled((Boolean) requestData.get("enabled"));
            settings.setWebhookUrl((String) requestData.get("webhookUrl"));
            settings.setSecret((String) requestData.get("secret"));
            
            // 处理通知级别数组
            if (requestData.containsKey("notifyLevels")) {
                List<String> notifyLevels = (List<String>) requestData.get("notifyLevels");
                settings.setNotifyLevels(objectMapper.writeValueAsString(notifyLevels));
            }
            
            // 处理免打扰时段数组
            if (requestData.containsKey("quietHours")) {
                List<String> quietHours = (List<String>) requestData.get("quietHours");
                settings.setQuietHours(objectMapper.writeValueAsString(quietHours));
            }
            
            // 记录处理后的数据
            log.info("处理后的钉钉设置数据 - enabled: {}, webhookUrl: {}, notifyLevels: {}, quietHours: {}", 
                settings.getEnabled(), settings.getWebhookUrl(), settings.getNotifyLevels(), 
                settings.getQuietHours());
            
            dingTalkSettingsRepository.save(settings);
            log.info("钉钉设置保存成功");
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ApiResponse.success("钉钉设置保存成功", null));
        } catch (Exception e) {
            log.error("保存钉钉设置失败: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ApiResponse.error("保存钉钉设置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 测试钉钉机器人
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Void>> testDingTalk(@RequestBody TestDingTalkRequest request) {
        try {
            log.info("收到钉钉机器人测试请求: {}", request);
            
            String webhookUrl = request.getWebhookUrl();
            String secret = request.getSecret();
            
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.error("钉钉webhook地址不能为空"));
            }
            
            boolean success = dingTalkService.sendTestMessage(webhookUrl, secret);
            
            if (success) {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.success("钉钉机器人测试成功", null));
            } else {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.error("钉钉机器人测试失败，请检查配置"));
            }
        } catch (Exception e) {
            log.error("钉钉机器人测试失败: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ApiResponse.error("钉钉机器人测试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送报警消息到钉钉
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendAlarmToDingTalk(@RequestBody Map<String, Object> alarmData) {
        try {
            log.info("收到发送钉钉报警消息请求: {}", alarmData);
            
            // 获取钉钉设置
            DingTalkSettings settings = dingTalkSettingsRepository.findByUserId(1L);
            if (settings == null || !settings.getEnabled()) {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.error("钉钉通知未启用"));
            }
            
            // 检查报警级别
            if (!isNotifyLevelEnabled(settings, (String) alarmData.get("level"))) {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.error("该报警级别未启用钉钉通知"));
            }
            
            // 检查免打扰时段
            if (isInQuietHours(settings)) {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.error("当前处于免打扰时段，不发送钉钉通知"));
            }
            
            // 构建报警消息
            String deviceName = (String) alarmData.get("deviceName");
            String alarmLevel = (String) alarmData.get("level");
            String alarmContent = (String) alarmData.get("content");
            String alarmTime = (String) alarmData.get("time");
            
            if (alarmTime == null) {
                alarmTime = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            
            boolean success = dingTalkService.sendAlarmMessage(
                settings.getWebhookUrl(), 
                settings.getSecret(), 
                deviceName, 
                alarmLevel, 
                alarmContent, 
                alarmTime
            );
            
            if (success) {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.success("钉钉消息发送成功", null));
            } else {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ApiResponse.error("发送钉钉消息失败"));
            }
        } catch (Exception e) {
            log.error("发送钉钉报警消息失败: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ApiResponse.error("发送钉钉报警消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查报警级别是否启用
     */
    private boolean isNotifyLevelEnabled(DingTalkSettings settings, String alarmLevel) {
        try {
            if (settings.getNotifyLevels() == null) {
                return true; // 默认所有级别都启用
            }
            
            List<String> enabledLevels = objectMapper.readValue(settings.getNotifyLevels(), List.class);
            return enabledLevels.contains(alarmLevel);
        } catch (Exception e) {
            log.error("检查报警级别失败: {}", e.getMessage());
            return true; // 出错时默认启用
        }
    }
    
    /**
     * 检查是否处于免打扰时段
     */
    private boolean isInQuietHours(DingTalkSettings settings) {
        try {
            if (settings.getQuietHours() == null) {
                return false; // 默认不启用免打扰
            }
            
            List<String> quietHours = objectMapper.readValue(settings.getQuietHours(), List.class);
            if (quietHours.size() < 2) {
                return false;
            }
            
            String startTime = quietHours.get(0);
            String endTime = quietHours.get(1);
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.of(now.toLocalDate(), 
                java.time.LocalTime.parse(startTime));
            LocalDateTime end = LocalDateTime.of(now.toLocalDate(), 
                java.time.LocalTime.parse(endTime));
            
            // 处理跨天的情况
            if (end.isBefore(start)) {
                end = end.plusDays(1);
            }
            
            return now.isAfter(start) && now.isBefore(end);
        } catch (Exception e) {
            log.error("检查免打扰时段失败: {}", e.getMessage());
            return false; // 出错时默认不启用免打扰
        }
    }
    
    /**
     * 创建默认钉钉设置
     */
    private DingTalkSettings createDefaultSettings() {
        DingTalkSettings settings = new DingTalkSettings();
        settings.setUserId(1L);
        settings.setEnabled(false);
        settings.setWebhookUrl("");
        settings.setSecret("");
        settings.setNotifyLevels("[\"high\", \"medium\"]");
        settings.setQuietHours("[\"22:00\", \"07:00\"]");
        return settings;
    }
    
    @Data
    public static class TestDingTalkRequest {
        private String webhookUrl;
        private String secret;
    }
}