package com.iot.fresh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.ResponseData;
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

import java.util.ArrayList;
import java.util.HashMap;
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
    public ResponseEntity<ResponseData<Map<String, Object>>> getEmailSettings() {
        try {
            EmailSettings settings = emailSettingsRepository.findByUserId(1L);
            if (settings == null) {
                // 创建默认设置
                settings = createDefaultSettings();
                emailSettingsRepository.save(settings);
            }
            
            // 转换为前端期望的格式
            Map<String, Object> result = new HashMap<>();
            result.put("id", settings.getId());
            result.put("userId", settings.getUserId());
            result.put("enabled", settings.getEnabled());
            
            // 将JSON字符串转换为数组
            ObjectMapper objectMapper = new ObjectMapper();
            if (settings.getEmailAddresses() != null) {
                result.put("emailAddresses", objectMapper.readValue(settings.getEmailAddresses(), List.class));
            } else {
                result.put("emailAddresses", new ArrayList<>());
            }
            
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
            
            result.put("pushFrequency", settings.getPushFrequency());
            result.put("createdAt", settings.getCreatedAt() != null ? settings.getCreatedAt().toString() : null);
            result.put("updatedAt", settings.getUpdatedAt() != null ? settings.getUpdatedAt().toString() : null);
            
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.success(result));
        } catch (Exception e) {
            log.error("获取邮件设置失败: {}", e.getMessage());
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.error("获取邮件设置失败"));
        }
    }
    
    /**
     * 保存邮件设置
     */
    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<Void>> saveEmailSettings(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("收到保存邮件设置请求: {}", requestData);
            
            EmailSettings settings = emailSettingsRepository.findByUserId(1L);
            if (settings == null) {
                settings = new EmailSettings();
                settings.setUserId(1L);
            }
            
            // 处理前端发送的数组数据，转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            
            settings.setEnabled((Boolean) requestData.get("enabled"));
            
            // 处理邮箱地址数组
            if (requestData.containsKey("emailAddresses")) {
                List<String> emailAddresses = (List<String>) requestData.get("emailAddresses");
                settings.setEmailAddresses(objectMapper.writeValueAsString(emailAddresses));
            }
            
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
            
            settings.setPushFrequency((String) requestData.get("pushFrequency"));
            
            // 记录处理后的数据
            log.info("处理后的设置数据 - enabled: {}, emailAddresses: {}, notifyLevels: {}, quietHours: {}, pushFrequency: {}", 
                settings.getEnabled(), settings.getEmailAddresses(), settings.getNotifyLevels(), 
                settings.getQuietHours(), settings.getPushFrequency());
            
            emailSettingsRepository.save(settings);
            log.info("邮件设置保存成功");
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ApiResponse.success("设置保存成功", null));
        } catch (Exception e) {
            log.error("保存邮件设置失败: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ApiResponse.error("保存邮件设置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取邮件模板
     */
    @GetMapping("/templates")
    public ResponseEntity<ResponseData<List<EmailTemplates>>> getEmailTemplates() {
        try {
            List<EmailTemplates> templates = emailTemplatesRepository.findAll();
            if (templates.isEmpty()) {
                // 创建默认模板
                templates = createDefaultTemplates();
                emailTemplatesRepository.saveAll(templates);
            }
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.success(templates));
        } catch (Exception e) {
            log.error("获取邮件模板失败: {}", e.getMessage());
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.error("获取邮件模板失败"));
        }
    }
    
    /**
     * 保存邮件模板
     */
    @PostMapping("/templates")
    public ResponseEntity<ResponseData<Void>> saveTemplates(@RequestBody Object requestData) {
        try {
            log.info("收到保存邮件模板请求，数据类型: {}, 数据内容: {}", 
                requestData != null ? requestData.getClass().getSimpleName() : "null", 
                requestData);
            
            // 检查数据类型
            if (requestData == null) {
                return ResponseEntity.ok(ResponseData.error("请求数据不能为空"));
            }
            
            // 尝试转换为Map
            if (!(requestData instanceof Map)) {
                return ResponseEntity.ok(ResponseData.error("请求数据格式不正确，期望Map格式"));
            }
            
            Map<?, ?> templatesMap = (Map<?, ?>) requestData;
            log.info("模板数据Map大小: {}", templatesMap.size());
            
            for (Map.Entry<?, ?> entry : templatesMap.entrySet()) {
                String templateType = entry.getKey().toString();
                Object templateDataObj = entry.getValue();
                
                log.info("处理模板类型: {}, 数据类型: {}", templateType, 
                    templateDataObj != null ? templateDataObj.getClass().getSimpleName() : "null");
                
                if (!(templateDataObj instanceof Map)) {
                    log.warn("模板数据格式不正确，跳过模板类型: {}", templateType);
                    continue;
                }
                
                Map<?, ?> templateData = (Map<?, ?>) templateDataObj;
                
                EmailTemplates template = emailTemplatesRepository.findByTemplateType(templateType);
                if (template == null) {
                    template = new EmailTemplates();
                    template.setTemplateType(templateType);
                }
                
                // 处理模板数据
                String subject = templateData.get("subject") != null ? templateData.get("subject").toString() : "";
                String content = templateData.get("content") != null ? templateData.get("content").toString() : "";
                
                log.info("处理模板类型: {}, 主题: {}, 内容长度: {}", templateType, subject, content.length());
                
                template.setTemplateSubject(subject);
                template.setTemplateContent(content);
                emailTemplatesRepository.save(template);
            }
            
            log.info("邮件模板保存成功");
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.success());
        } catch (Exception e) {
            log.error("保存邮件模板失败: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.error("保存邮件模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送自定义邮件
     */
    @PostMapping("/send")
    public ResponseEntity<ResponseData<Void>> sendEmail(@RequestBody SendEmailRequest request) {
        try {
            log.info("收到发送邮件请求: {}", request);
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.success());
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.error("发送邮件失败"));
        }
    }
    
    /**
     * 测试邮件发送
     */
    @PostMapping("/test")
    public ResponseEntity<ResponseData<Void>> testEmail(@RequestBody TestEmailRequest request) {
        try {
            boolean success = emailNotificationService.sendTestEmail(request.getEmailAddress());
            if (success) {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ResponseData.success());
            } else {
                return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(ResponseData.error("测试邮件发送失败"));
            }
        } catch (Exception e) {
            log.error("测试邮件发送失败: {}", e.getMessage());
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                .body(ResponseData.error("测试邮件发送失败"));
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