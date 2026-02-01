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
    public ResponseEntity<ResponseData<SmsSettingsDto>> getSettings() {
        try {
            // 默认获取管理员用户的设置
            SmsSettings settings = smsSettingsRepository.findByUserId(1L);
            SmsSettingsDto dto;
            
            if (settings == null) {
                // 如果没有设置，返回默认设置
                dto = new SmsSettingsDto();
            } else {
                // 将实体转换为DTO
                dto = SmsSettingsDto.fromEntity(settings);
            }
            
            return ResponseEntity.ok(ResponseData.success(dto));
        } catch (Exception e) {
            log.error("获取短信设置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseData.error("获取短信设置失败"));
        }
    }
    
    /**
     * 保存短信设置
     */
    @PostMapping("/settings")
    public ResponseEntity<ResponseData<Void>> saveSettings(@RequestBody SmsSettingsDto request) {
        try {
            log.info("收到保存短信设置请求: {}", request);
            
            // 验证数据
            if (request == null) {
                return ResponseEntity.ok(ResponseData.error("设置数据不能为空"));
            }
            
            // 设置默认值
            if (request.getEnabled() == null) {
                request.setEnabled(false);
            }
            if (request.getPhoneNumbers() == null) {
                request.setPhoneNumbers(Arrays.asList("13800138000"));
            }
            if (request.getNotifyLevels() == null) {
                request.setNotifyLevels(Arrays.asList("high", "medium"));
            }
            if (request.getQuietHours() == null) {
                request.setQuietHours(new String[]{"22:00", "07:00"});
            }
            if (request.getPushFrequency() == null) {
                request.setPushFrequency("immediate");
            }
            
            // 转换为实体并保存
            SmsSettings settings = request.toEntity();
            settings.setUserId(1L); // 默认管理员用户
            
            smsSettingsRepository.save(settings);
            
            log.info("短信设置保存成功");
            return ResponseEntity.ok(ResponseData.success());
        } catch (Exception e) {
            log.error("保存短信设置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseData.error("保存短信设置失败: " + e.getMessage()));
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
     * 发送测试短信
     */
    @PostMapping("/test")
    public ResponseEntity<ResponseData<Void>> testSms(@RequestBody TestSmsRequest request) {
        try {
            boolean success = smsNotificationService.sendTestSms(request.getPhoneNumber());
            
            if (success) {
                return ResponseEntity.ok(ResponseData.success());
            } else {
                return ResponseEntity.ok(ResponseData.error("测试短信发送失败"));
            }
        } catch (Exception e) {
            log.error("发送测试短信失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseData.error("发送测试短信失败"));
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