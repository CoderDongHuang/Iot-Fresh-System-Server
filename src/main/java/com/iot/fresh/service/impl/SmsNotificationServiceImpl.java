package com.iot.fresh.service.impl;

import com.iot.fresh.entity.*;
import com.iot.fresh.repository.*;
import com.iot.fresh.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class SmsNotificationServiceImpl {
    
    @Autowired
    private SmsSettingsRepository smsSettingsRepository;
    
    @Autowired
    private SmsTemplatesRepository smsTemplatesRepository;
    
    @Autowired
    private SmsLogsRepository smsLogsRepository;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 发送报警短信
     */
    public void sendAlarmSms(Alarm alarm) {
        try {
            // 1. 获取短信设置（默认使用管理员用户设置）
            SmsSettings settings = smsSettingsRepository.findByUserId(1L);
            if (settings == null || !settings.getEnabled()) {
                log.info("短信通知未启用或设置不存在");
                return;
            }
            
            // 2. 检查报警级别
            if (!isNotifyLevelEnabled(settings, alarm.getAlarmLevel())) {
                log.info("报警级别 {} 未启用短信通知", alarm.getAlarmLevel());
                return;
            }
            
            // 3. 检查免打扰时段
            if (isInQuietHours(settings)) {
                log.info("当前处于免打扰时段，不发送短信");
                return;
            }
            
            // 4. 获取短信模板
            SmsTemplates template = smsTemplatesRepository.findByTemplateType(alarm.getAlarmLevel());
            if (template == null) {
                log.error("未找到对应级别的短信模板: {}", alarm.getAlarmLevel());
                return;
            }
            
            // 5. 替换模板变量
            String message = replaceTemplateVariables(template.getTemplateContent(), alarm);
            
            // 6. 解析手机号列表
            List<String> phoneNumbers = parsePhoneNumbers(settings.getPhoneNumbers());
            if (phoneNumbers.isEmpty()) {
                log.error("未配置接收短信的手机号");
                return;
            }
            
            // 7. 发送短信
            for (String phoneNumber : phoneNumbers) {
                boolean success = smsService.sendSms(phoneNumber, message, alarm.getAlarmLevel());
                
                // 8. 记录发送日志
                SmsLogs smsLog = new SmsLogs();
                smsLog.setAlarmId(alarm.getId());
                smsLog.setPhoneNumber(phoneNumber);
                smsLog.setMessageContent(message);
                smsLog.setTemplateType(alarm.getAlarmLevel());
                smsLog.setSendStatus(success ? "success" : "failed");
                if (!success) {
                    smsLog.setErrorMessage("短信发送失败");
                }
                smsLogsRepository.save(smsLog);
                
                log.info("短信发送结果 - 手机号: {}, 状态: {}, 报警ID: {}", 
                    phoneNumber, success ? "成功" : "失败", alarm.getId());
            }
            
        } catch (Exception e) {
            log.error("发送报警短信异常 - 报警ID: {}, 错误: {}", alarm.getId(), e.getMessage());
        }
    }
    
    /**
     * 检查是否启用该级别的通知
     */
    private boolean isNotifyLevelEnabled(SmsSettings settings, String alarmLevel) {
        try {
            List<String> notifyLevels = objectMapper.readValue(
                settings.getNotifyLevels(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            return notifyLevels.contains(alarmLevel);
        } catch (Exception e) {
            log.error("解析通知级别设置失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查是否处于免打扰时段
     */
    private boolean isInQuietHours(SmsSettings settings) {
        try {
            List<String> quietHours = objectMapper.readValue(
                settings.getQuietHours(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            
            if (quietHours.size() >= 2) {
                LocalTime now = LocalTime.now();
                LocalTime start = LocalTime.parse(quietHours.get(0));
                LocalTime end = LocalTime.parse(quietHours.get(1));
                
                if (start.isBefore(end)) {
                    // 正常时段：22:00-07:00
                    return now.isAfter(start) && now.isBefore(end);
                } else {
                    // 跨天时段：23:00-08:00
                    return now.isAfter(start) || now.isBefore(end);
                }
            }
        } catch (Exception e) {
            log.error("解析免打扰时段设置失败: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 替换模板变量
     */
    private String replaceTemplateVariables(String template, Alarm alarm) {
        return template
            .replace("{device}", alarm.getDeviceName() != null ? alarm.getDeviceName() : "未知设备")
            .replace("{level}", getLevelDescription(alarm.getAlarmLevel()))
            .replace("{content}", alarm.getMessage() != null ? alarm.getMessage() : "无报警内容");
    }
    
    /**
     * 解析手机号列表
     */
    private List<String> parsePhoneNumbers(String phoneNumbersJson) {
        try {
            return objectMapper.readValue(
                phoneNumbersJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (Exception e) {
            log.error("解析手机号列表失败: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 获取级别描述
     */
    private String getLevelDescription(String level) {
        switch (level.toLowerCase()) {
            case "high":
                return "紧急";
            case "medium":
                return "重要";
            case "low":
                return "一般";
            default:
                return level;
        }
    }
    
    /**
     * 测试短信发送
     */
    public boolean sendTestSms(String phoneNumber) {
        try {
            String testMessage = "【物联网生鲜品储运系统】测试短信，系统运行正常。";
            boolean success = smsService.sendSms(phoneNumber, testMessage, "high");
            
            if (success) {
                log.info("测试短信发送成功 - 手机号: {}", phoneNumber);
            } else {
                log.error("测试短信发送失败 - 手机号: {}", phoneNumber);
            }
            
            return success;
        } catch (Exception e) {
            log.error("发送测试短信异常 - 手机号: {}, 错误: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
}