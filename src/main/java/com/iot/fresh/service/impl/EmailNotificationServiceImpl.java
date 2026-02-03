package com.iot.fresh.service.impl;

import com.iot.fresh.entity.Alarm;
import com.iot.fresh.entity.EmailLogs;
import com.iot.fresh.entity.EmailSettings;
import com.iot.fresh.entity.EmailTemplates;
import com.iot.fresh.repository.EmailLogsRepository;
import com.iot.fresh.repository.EmailSettingsRepository;
import com.iot.fresh.repository.EmailTemplatesRepository;
import com.iot.fresh.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class EmailNotificationServiceImpl {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EmailSettingsRepository emailSettingsRepository;
    
    @Autowired
    private EmailTemplatesRepository emailTemplatesRepository;
    
    @Autowired
    private EmailLogsRepository emailLogsRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 发送报警邮件
     */
    public void sendAlarmEmail(Alarm alarm) {
        try {
            // 1. 获取邮件设置（默认使用管理员用户设置）
            EmailSettings settings = emailSettingsRepository.findByUserId(1L);
            if (settings == null || !settings.getEnabled()) {
                log.info("邮件通知未启用或设置不存在");
                return;
            }
            
            // 2. 检查报警级别
            if (!isNotifyLevelEnabled(settings, alarm.getAlarmLevel())) {
                log.info("报警级别 {} 未启用邮件通知", alarm.getAlarmLevel());
                return;
            }
            
            // 3. 检查免打扰时段
            if (isInQuietHours(settings)) {
                log.info("当前处于免打扰时段，不发送邮件");
                return;
            }
            
            // 4. 获取邮件模板
            EmailTemplates template = emailTemplatesRepository.findByTemplateType(alarm.getAlarmLevel());
            if (template == null) {
                log.error("未找到对应级别的邮件模板: {}", alarm.getAlarmLevel());
                return;
            }
            
            // 5. 替换模板变量
            String subject = replaceTemplateVariables(template.getTemplateSubject(), alarm);
            String content = replaceTemplateVariables(template.getTemplateContent(), alarm);
            
            // 6. 解析邮箱地址列表
            List<String> emailAddresses = parseEmailAddresses(settings.getEmailAddresses());
            if (emailAddresses.isEmpty()) {
                log.error("未配置接收邮件的邮箱地址");
                return;
            }
            
            // 7. 发送邮件
            for (String emailAddress : emailAddresses) {
                boolean success = emailService.sendAlertEmail(emailAddress, subject, content);
                
                // 8. 记录发送日志
                EmailLogs emailLog = new EmailLogs();
                emailLog.setAlarmId(alarm.getId());
                emailLog.setEmailAddress(emailAddress);
                emailLog.setEmailSubject(subject);
                emailLog.setEmailContent(content);
                emailLog.setTemplateType(alarm.getAlarmLevel());
                emailLog.setSendStatus(success ? "success" : "failed");
                if (!success) {
                    emailLog.setErrorMessage("邮件发送失败");
                }
                emailLogsRepository.save(emailLog);
                
                log.info("邮件发送结果 - 邮箱地址: {}, 状态: {}, 报警ID: {}", 
                    emailAddress, success ? "成功" : "失败", alarm.getId());
            }
            
        } catch (Exception e) {
            log.error("发送报警邮件异常 - 报警ID: {}, 错误: {}", alarm.getId(), e.getMessage());
        }
    }
    
    /**
     * 检查是否启用该报警级别的通知
     */
    private boolean isNotifyLevelEnabled(EmailSettings settings, String alarmLevel) {
        try {
            List<String> notifyLevels = objectMapper.readValue(
                settings.getNotifyLevels(), 
                new TypeReference<List<String>>() {}
            );
            return notifyLevels.contains(alarmLevel);
        } catch (Exception e) {
            log.error("解析通知级别设置失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查是否在免打扰时段
     */
    private boolean isInQuietHours(EmailSettings settings) {
        try {
            List<String> quietHours = objectMapper.readValue(
                settings.getQuietHours(), 
                new TypeReference<List<String>>() {}
            );
            
            if (quietHours.size() >= 2) {
                LocalTime start = LocalTime.parse(quietHours.get(0));
                LocalTime end = LocalTime.parse(quietHours.get(1));
                LocalTime now = LocalTime.now();
                
                if (start.isBefore(end)) {
                    return now.isAfter(start) && now.isBefore(end);
                } else {
                    return now.isAfter(start) || now.isBefore(end);
                }
            }
            return false;
        } catch (Exception e) {
            log.error("解析免打扰时段设置失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 替换模板变量
     */
    private String replaceTemplateVariables(String template, Alarm alarm) {
        // 为报警级别和报警类型添加中文解释
        String levelWithChinese = getLevelChineseExplanation(alarm.getAlarmLevel());
        String typeWithChinese = getTypeChineseExplanation(alarm.getAlarmType());
        
        // 格式化时间，去掉T和多余的位数
        String formattedTime = formatDateTime(alarm.getCreatedAt());
        
        return template
            .replace("{device}", alarm.getDeviceName())
            .replace("{level}", levelWithChinese)
            .replace("{content}", alarm.getMessage())
            .replace("{time}", formattedTime)
            .replace("{type}", typeWithChinese);
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        // 使用中文格式：年-月-日 时:分:秒
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
    
    /**
     * 获取报警级别中文解释
     */
    private String getLevelChineseExplanation(String level) {
        switch (level.toLowerCase()) {
            case "high": return "high(紧急)";
            case "medium": return "medium(重要)";
            case "low": return "low(一般)";
            default: return level;
        }
    }
    
    /**
     * 获取报警类型中文解释
     */
    private String getTypeChineseExplanation(String type) {
        switch (type.toLowerCase()) {
            case "temperature": return "temperature(温度异常)";
            case "humidity": return "humidity(湿度异常)";
            case "pressure": return "pressure(压力异常)";
            case "power": return "power(电源异常)";
            case "connection": return "connection(连接异常)";
            default: return type;
        }
    }
    
    /**
     * 解析邮箱地址列表
     */
    private List<String> parseEmailAddresses(String emailAddressesJson) {
        try {
            return objectMapper.readValue(
                emailAddressesJson, 
                new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            log.error("解析邮箱地址列表失败: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 测试邮件发送
     */
    public boolean sendTestEmail(String emailAddress) {
        try {
            boolean success = emailService.sendTestEmail(emailAddress);
            
            if (success) {
                log.info("测试邮件发送成功 - 邮箱地址: {}", emailAddress);
            } else {
                log.error("测试邮件发送失败 - 邮箱地址: {}", emailAddress);
            }
            
            return success;
        } catch (Exception e) {
            log.error("发送测试邮件异常 - 邮箱地址: {}, 错误: {}", emailAddress, e.getMessage());
            return false;
        }
    }
}