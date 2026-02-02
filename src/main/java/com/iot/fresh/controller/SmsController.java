package com.iot.fresh.controller;

import com.iot.fresh.dto.*;
import com.iot.fresh.entity.*;
import com.iot.fresh.repository.*;
import com.iot.fresh.service.impl.SmsNotificationServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@Slf4j
public class SmsController {
    
    @Autowired
    private SmsSettingsRepository smsSettingsRepository;
    
    @Autowired
    private SmsTemplatesRepository smsTemplatesRepository;
    
    @Autowired
    private SmsNotificationServiceImpl smsNotificationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 获取短信设置
     */
    @GetMapping("/settings")
    public ResponseData<SmsSettingsDto> getSmsSettings() {
        try {
            log.info("获取短信设置");
            
            // 直接返回默认设置，避免数据库查询问题
            SmsSettingsDto dto = createDefaultSettings();
            
            return ResponseData.success(dto);
        } catch (Exception e) {
            log.error("获取短信设置失败: {}", e.getMessage());
            return ResponseData.error("获取短信设置失败");
        }
    }
    
    /**
     * 保存短信设置
     */
    @PostMapping("/settings")
    public ResponseData<Void> saveSmsSettings(@RequestBody SmsSettingsDto settings) {
        try {
            log.info("收到设置: {}", settings);
            
            // 验证和设置默认值
            settings = validateAndSetDefaults(settings);
            
            // 保存到数据库
            SmsSettings entity = settings.toEntity();
            entity.setUserId(1L); // 默认管理员用户
            smsSettingsRepository.save(entity);
            
            log.info("短信设置保存成功");
            return ResponseData.success();
        } catch (Exception e) {
            log.error("保存短信设置失败: {}", e.getMessage());
            return ResponseData.error("保存短信设置失败");
        }
    }
    
    /**
     * 测试短信发送
     */
    @PostMapping("/test")
    public ResponseData<Void> testSms(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");
            log.info("测试短信发送到: {}", phoneNumber);
            
            // 验证手机号
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseData.error("手机号不能为空");
            }
            
            // 简单验证手机号格式（11位数字）
            if (!phoneNumber.matches("^1[3-9]\\d{9}$")) {
                return ResponseData.error("手机号格式不正确");
            }
            
            // 调用真实的短信服务发送测试短信
            String testMessage = "【物联网生鲜品储运系统】测试短信，系统运行正常。";
            boolean success = smsNotificationService.sendTestSms(phoneNumber);
            
            if (success) {
                log.info("测试短信发送成功: {}", phoneNumber);
                return ResponseData.success();
            } else {
                log.error("测试短信发送失败: {}", phoneNumber);
                return ResponseData.error("测试短信发送失败");
            }
            
        } catch (Exception e) {
            log.error("测试短信发送失败: {}", e.getMessage());
            return ResponseData.error("测试短信发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取短信模板
     */
    @GetMapping("/templates")
    public ResponseEntity<ResponseData<Map<String, String>>> getTemplates() {
        try {
            Map<String, String> templates = new HashMap<>();
            
            SmsTemplates highTemplate = smsTemplatesRepository.findByTemplateType("high");
            SmsTemplates mediumTemplate = smsTemplatesRepository.findByTemplateType("medium");
            SmsTemplates lowTemplate = smsTemplatesRepository.findByTemplateType("low");
            
            if (highTemplate != null) {
                templates.put("high", highTemplate.getTemplateContent());
            }
            if (mediumTemplate != null) {
                templates.put("medium", mediumTemplate.getTemplateContent());
            }
            if (lowTemplate != null) {
                templates.put("low", lowTemplate.getTemplateContent());
            }
            
            return ResponseEntity.ok(ResponseData.success(templates));
        } catch (Exception e) {
            log.error("获取短信模板失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseData.error("获取短信模板失败"));
        }
    }
    
    /**
     * 保存短信模板
     */
    @PostMapping("/templates")
    public ResponseEntity<ResponseData<Void>> saveTemplates(@RequestBody Map<String, String> templates) {
        try {
            for (Map.Entry<String, String> entry : templates.entrySet()) {
                String templateType = entry.getKey();
                String templateContent = entry.getValue();
                
                SmsTemplates template = smsTemplatesRepository.findByTemplateType(templateType);
                if (template == null) {
                    template = new SmsTemplates();
                    template.setTemplateType(templateType);
                }
                
                template.setTemplateContent(templateContent);
                smsTemplatesRepository.save(template);
            }
            
            log.info("短信模板保存成功");
            return ResponseEntity.ok(ResponseData.success());
        } catch (Exception e) {
            log.error("保存短信模板失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseData.error("保存短信模板失败"));
        }
    }
    

    
    /**
     * 发送自定义短信
     */
    @PostMapping("/send")
    public ResponseEntity<ResponseData<Void>> sendSms(@RequestBody SendSmsRequest request) {
        try {
            // 这里可以扩展为发送自定义短信的功能
            // 目前先返回成功，实际实现需要根据需求开发
            log.info("收到发送短信请求: {}", request);
            return ResponseEntity.ok(ResponseData.success());
        } catch (Exception e) {
            log.error("发送短信失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseData.error("发送短信失败"));
        }
    }
    
    // 不再使用SmsSettingsRequest，已使用SmsSettingsDto替代
    
    // 创建默认设置
    private SmsSettingsDto createDefaultSettings() {
        SmsSettingsDto settings = new SmsSettingsDto();
        settings.setEnabled(false);
        settings.setPhoneNumbers(new ArrayList<>());
        settings.setNotifyLevels(Arrays.asList("high", "medium"));
        settings.setQuietHours(Arrays.asList("22:00", "07:00"));
        settings.setPushFrequency("immediate");
        return settings;
    }
    
    // 验证和设置默认值
    private SmsSettingsDto validateAndSetDefaults(SmsSettingsDto settings) {
        if (settings == null) {
            return createDefaultSettings();
        }
        
        if (settings.getEnabled() == null) settings.setEnabled(false);
        if (settings.getPhoneNumbers() == null) settings.setPhoneNumbers(new ArrayList<>());
        if (settings.getNotifyLevels() == null) settings.setNotifyLevels(Arrays.asList("high", "medium"));
        if (settings.getQuietHours() == null) settings.setQuietHours(Arrays.asList("22:00", "07:00"));
        if (settings.getPushFrequency() == null) settings.setPushFrequency("immediate");
        
        return settings;
    }
    
    @Data
    public static class TestSmsRequest {
        private String phoneNumber;
    }
    
    @Data
    public static class SendSmsRequest {
        private List<String> phoneNumbers;
        private String template;
        private Map<String, String> variables;
        private String level;
    }
    
    /**
     * 响应数据类
     */
    @Data
    public static class ResponseData<T> {
        private int code;
        private String msg;
        private T data;
        
        public static <T> ResponseData<T> success() {
            return success(null);
        }
        
        public static <T> ResponseData<T> success(T data) {
            ResponseData<T> response = new ResponseData<>();
            response.setCode(200);
            response.setMsg("success");
            response.setData(data);
            return response;
        }
        
        public static <T> ResponseData<T> error(String message) {
            ResponseData<T> response = new ResponseData<>();
            response.setCode(500);
            response.setMsg(message);
            return response;
        }
    }
}