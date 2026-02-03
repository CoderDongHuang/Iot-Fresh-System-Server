package com.iot.fresh.controller;

import com.iot.fresh.dto.*;
import com.iot.fresh.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@Slf4j
public class NotificationController {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationSettingsService settingsService;
    
    /**
     * 发送邮件通知
     */
    @PostMapping("/email")
    public ResponseEntity<?> sendEmailNotification(@RequestBody EmailNotifyRequest request) {
        try {
            log.info("收到邮件通知请求: {}", request);
            
            if (request.getEmailAddresses() != null && !request.getEmailAddresses().isEmpty()) {
                // 使用模板和变量构建邮件内容
                String subject = buildMessageFromTemplate(request.getSubject(), request.getVariables());
                String content = buildMessageFromTemplate(request.getContent(), request.getVariables());
                
                log.info("发送邮件 - 主题: {}, 收件人数量: {}", subject, request.getEmailAddresses().size());
                
                // 调用邮件服务发送邮件给所有收件人
                for (String emailAddress : request.getEmailAddresses()) {
                    boolean success = emailService.sendAlertEmail(emailAddress, subject, content);
                    if (!success) {
                        log.warn("邮件发送失败: {}", emailAddress);
                    }
                }
                
                return ResponseEntity.ok(ResponseData.success());
            }
            return ResponseEntity.badRequest().body(ResponseData.error("邮箱地址不能为空"));
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ResponseData.error("邮件发送失败: " + e.getMessage()));
        }
    }
    
    private String buildMessageFromTemplate(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            template = "【物联网系统】报警通知：请及时处理。";
        }
        
        String message = template;
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = "{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                message = message.replace(key, value);
            }
        }
        return message;
    }
    
    /**
     * 获取通知设置
     */
    @GetMapping("/settings")
    public ResponseEntity<?> getNotificationSettings() {
        try {
            // 这里简化实现，实际应该根据当前登录用户获取设置
            NotificationSettings settings = settingsService.getUserSettings(1L);
            return ResponseEntity.ok(ResponseData.success(settings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseData.error("获取通知设置失败: " + e.getMessage()));
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
            return ResponseEntity.ok(ResponseData.success());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseData.error("保存通知设置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取邮件模板
     */
    @GetMapping("/email-templates")
    public ResponseEntity<?> getEmailTemplates() {
        try {
            Map<String, String> templates = settingsService.getEmailTemplates();
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取邮件模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 保存邮件模板
     */
    @PostMapping("/email-templates")
    public ResponseEntity<?> saveEmailTemplates(@RequestBody Map<String, String> templates) {
        try {
            settingsService.saveEmailTemplates(templates);
            return ResponseEntity.ok(ApiResponse.success("模板保存成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("保存邮件模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 测试邮件
     */
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody TestEmailRequest request) {
        try {
            log.info("发送测试邮件到: {}", request.getEmailAddress());
            
            String subject = "【物联网系统】测试邮件";
            String content = "这是一封测试邮件，用于验证邮件发送功能是否正常。\n\n" +
                           "发送时间: " + java.time.LocalDateTime.now() + "\n" +
                           "系统: 物联网生鲜品储运系统";
            
            boolean success = emailService.sendAlertEmail(request.getEmailAddress(), subject, content);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("测试邮件发送成功"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("测试邮件发送失败"));
            }
        } catch (Exception e) {
            log.error("测试邮件发送失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("测试邮件发送失败: " + e.getMessage()));
        }
    }
}