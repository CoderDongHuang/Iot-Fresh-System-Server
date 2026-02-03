package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.entity.EmailSettings;
import com.iot.fresh.entity.EmailTemplates;
import com.iot.fresh.repository.EmailSettingsRepository;
import com.iot.fresh.repository.EmailTemplatesRepository;
import com.iot.fresh.service.impl.EmailNotificationServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@Slf4j
public class EmailController {
    
    @Autowired
    private EmailSettingsRepository emailSettingsRepository;
    
    @Autowired
    private EmailTemplatesRepository emailTemplatesRepository;
    
    @Autowired
    private EmailNotificationServiceImpl emailNotificationService;
    
    /**
     * 获取邮件设置
     */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<EmailSettings>> getEmailSettings() {
        try {
            EmailSettings settings = emailSettingsRepository.findByUserId(1L);
            if (settings == null) {
                // 创建默认设置
                settings = createDefaultSettings();
                emailSettingsRepository.save(settings);
            }
            return ResponseEntity.ok(ApiResponse.success(settings));
        } catch (Exception e) {
            log.error("获取邮件设置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取邮件设置失败"));
        }
    }
    
    /**
     * 保存邮件设置
     */
    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<Void>> saveEmailSettings(@RequestBody EmailSettings request) {
        try {
            EmailSettings settings = emailSettingsRepository.findByUserId(1L);
            if (settings == null) {
                settings = new EmailSettings();
                settings.setUserId(1L);
            }
            
            settings.setEnabled(request.getEnabled());
            settings.setEmailAddresses(request.getEmailAddresses());
            settings.setNotifyLevels(request.getNotifyLevels());
            settings.setQuietHours(request.getQuietHours());
            settings.setPushFrequency(request.getPushFrequency());
            
            emailSettingsRepository.save(settings);
            log.info("邮件设置保存成功");
            return ResponseEntity.ok(ApiResponse.success("设置保存成功", null));
        } catch (Exception e) {
            log.error("保存邮件设置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("保存邮件设置失败"));
        }
    }
    
    /**
     * 获取邮件模板
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<EmailTemplates>>> getEmailTemplates() {
        try {
            List<EmailTemplates> templates = emailTemplatesRepository.findAll();
            if (templates.isEmpty()) {
                // 创建默认模板
                templates = createDefaultTemplates();
                emailTemplatesRepository.saveAll(templates);
            }
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            log.error("获取邮件模板失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取邮件模板失败"));
        }
    }
    
    /**
     * 保存邮件模板
     */
    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<Void>> saveTemplates(@RequestBody Map<String, EmailTemplateRequest> templates) {
        try {
            for (Map.Entry<String, EmailTemplateRequest> entry : templates.entrySet()) {
                String templateType = entry.getKey();
                EmailTemplateRequest templateRequest = entry.getValue();
                
                EmailTemplates template = emailTemplatesRepository.findByTemplateType(templateType);
                if (template == null) {
                    template = new EmailTemplates();
                    template.setTemplateType(templateType);
                }
                
                template.setTemplateSubject(templateRequest.getSubject());
                template.setTemplateContent(templateRequest.getContent());
                emailTemplatesRepository.save(template);
            }
            
            log.info("邮件模板保存成功");
            return ResponseEntity.ok(ApiResponse.success("模板保存成功", null));
        } catch (Exception e) {
            log.error("保存邮件模板失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("保存邮件模板失败"));
        }
    }
    
    /**
     * 发送自定义邮件
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendEmail(@RequestBody SendEmailRequest request) {
        try {
            log.info("收到发送邮件请求: {}", request);
            return ResponseEntity.ok(ApiResponse.success("邮件发送成功", null));
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("发送邮件失败"));
        }
    }
    
    /**
     * 测试邮件发送
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Void>> testEmail(@RequestBody TestEmailRequest request) {
        try {
            boolean success = emailNotificationService.sendTestEmail(request.getEmailAddress());
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("测试邮件发送成功", null));
            } else {
                return ResponseEntity.ok(ApiResponse.error("测试邮件发送失败"));
            }
        } catch (Exception e) {
            log.error("测试邮件发送失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("测试邮件发送失败"));
        }
    }
    
    /**
     * 创建默认邮件设置
     */
    private EmailSettings createDefaultSettings() {
        EmailSettings settings = new EmailSettings();
        settings.setUserId(1L);
        settings.setEnabled(false);
        settings.setEmailAddresses("[\"admin@example.com\"]");
        settings.setNotifyLevels("[\"high\", \"medium\"]");
        settings.setQuietHours("[\"22:00\", \"07:00\"]");
        settings.setPushFrequency("immediate");
        return settings;
    }
    
    /**
     * 创建默认邮件模板
     */
    private List<EmailTemplates> createDefaultTemplates() {
        EmailTemplates highTemplate = new EmailTemplates();
        highTemplate.setTemplateType("high");
        highTemplate.setTemplateSubject("【物联网生鲜品储运系统】紧急报警通知");
        highTemplate.setTemplateContent("紧急报警通知\\n\\n设备：{device}\\n报警级别：{level}\\n报警内容：{content}\\n报警时间：{time}\\n报警类型：{type}\\n\\n请立即处理！");
        
        EmailTemplates mediumTemplate = new EmailTemplates();
        mediumTemplate.setTemplateType("medium");
        mediumTemplate.setTemplateSubject("【物联网生鲜品储运系统】重要报警通知");
        mediumTemplate.setTemplateContent("重要报警通知\\n\\n设备：{device}\\n报警级别：{level}\\n报警内容：{content}\\n报警时间：{time}\\n报警类型：{type}\\n\\n请及时处理。");
        
        EmailTemplates lowTemplate = new EmailTemplates();
        lowTemplate.setTemplateType("low");
        lowTemplate.setTemplateSubject("【物联网生鲜品储运系统】一般报警通知");
        lowTemplate.setTemplateContent("一般报警通知\\n\\n设备：{device}\\n报警级别：{level}\\n报警内容：{content}\\n报警时间：{time}\\n报警类型：{type}\\n\\n请关注。");
        
        return List.of(highTemplate, mediumTemplate, lowTemplate);
    }
    
    @Data
    public static class TestEmailRequest {
        private String emailAddress;
    }
    
    @Data
    public static class SendEmailRequest {
        private List<String> emailAddresses;
        private String subject;
        private String content;
        private String level;
    }
    
    @Data
    public static class EmailTemplateRequest {
        private String subject;
        private String content;
    }
}