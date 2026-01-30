package com.iot.fresh.controller;

import com.iot.fresh.dto.*;
import com.iot.fresh.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private NotificationSettingsService settingsService;
    
    /**
     * 发送短信通知
     */
    @PostMapping("/sms")
    public ResponseEntity<?> sendSmsNotification(@RequestBody SmsNotifyRequest request) {
        try {
            smsService.sendTemplateSms(request.getPhoneNumbers(), request.getTemplate(), request.getVariables());
            return ResponseEntity.ok(ApiResponse.success("短信发送成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("短信发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取通知设置
     */
    @GetMapping("/settings")
    public ResponseEntity<?> getNotificationSettings() {
        try {
            // 这里简化实现，实际应该根据当前登录用户获取设置
            NotificationSettings settings = settingsService.getUserSettings(1L);
            return ResponseEntity.ok(ApiResponse.success(settings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取通知设置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 保存通知设置
     */
    @PostMapping("/settings")
    public ResponseEntity<?> saveNotificationSettings(@RequestBody NotificationSettings settings) {
        try {
            // 这里简化实现，实际应该根据当前登录用户保存设置
            settingsService.saveUserSettings(1L, settings);
            return ResponseEntity.ok(ApiResponse.success("设置保存成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("保存通知设置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取短信模板
     */
    @GetMapping("/templates")
    public ResponseEntity<?> getSmsTemplates() {
        try {
            Map<String, String> templates = settingsService.getSmsTemplates();
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取短信模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 保存短信模板
     */
    @PostMapping("/templates")
    public ResponseEntity<?> saveSmsTemplates(@RequestBody Map<String, String> templates) {
        try {
            settingsService.saveSmsTemplates(templates);
            return ResponseEntity.ok(ApiResponse.success("模板保存成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("保存短信模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 测试短信
     */
    @PostMapping("/test-sms")
    public ResponseEntity<?> testSms(@RequestBody TestSmsRequest request) {
        try {
            smsService.sendTestSms(request.getPhoneNumber(), request.getMessage());
            return ResponseEntity.ok(ApiResponse.success("测试短信发送成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("测试短信发送失败: " + e.getMessage()));
        }
    }
}