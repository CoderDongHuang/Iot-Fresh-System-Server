package com.iot.fresh.service;

import com.iot.fresh.dto.NotificationSettings;
import com.iot.fresh.entity.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnhancedAlarmService {
    
    @Autowired
    private AlarmPushService pushService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private AlarmService alarmService;
    
    @Autowired
    private NotificationSettingsService settingsService;
    
    public void processAlarmWithNotification(Alarm alarm, String action, String operator) {
        // 1. 处理报警
        alarm.setStatus("resolved");
        // 这里需要保存报警，但实际应该调用alarmService的方法
        
        // 2. 添加历史记录
        alarmService.addAlarmHistory(alarm.getId(), action, operator, "通过增强服务处理");
        
        // 3. 发送通知（根据设置）
        sendAlarmNotifications(alarm);
    }
    
    private void sendAlarmNotifications(Alarm alarm) {
        // 获取当前用户的通知设置（这里简化实现，实际应该根据用户ID获取）
        NotificationSettings settings = settingsService.getDefaultSettings();
        
        // 检查是否应该发送通知
        if (shouldSendNotification(alarm, settings)) {
            // 发送优先推送
            if (settings.isPopupEnabled()) {
                pushService.sendPriorityAlarm(alarm);
            }
            
            // 发送短信
            if (settings.isSmsEnabled() && isHighPriority(alarm.getAlarmLevel())) {
                sendSmsNotification(alarm, settings);
            }
        }
    }
    
    private boolean shouldSendNotification(Alarm alarm, NotificationSettings settings) {
        // 检查报警级别是否在通知设置中
        if (settings.getNotifyLevels() != null && 
            !settings.getNotifyLevels().contains(alarm.getAlarmLevel())) {
            return false;
        }
        
        // 检查静默时段
        if (isQuietHours(settings.getQuietHours())) {
            return false;
        }
        
        return true;
    }
    
    private boolean isQuietHours(String[] quietHours) {
        if (quietHours == null || quietHours.length < 2) {
            return false;
        }
        
        // 简化实现：实际应该检查当前时间是否在静默时段内
        // 这里返回false表示不在静默时段
        return false;
    }
    
    private boolean isHighPriority(String level) {
        return "high".equals(level);
    }
    
    private void sendSmsNotification(Alarm alarm, NotificationSettings settings) {
        String template = getTemplateByLevel(alarm.getAlarmLevel());
        String message = createMessageFromTemplate(template, alarm);
        
        if (settings.getPhoneNumbers() != null && !settings.getPhoneNumbers().isEmpty()) {
            List<String> phoneNumbers = Arrays.asList(settings.getPhoneNumbers().split(","));
            for (String phoneNumber : phoneNumbers) {
                smsService.sendSms(phoneNumber, message, alarm.getAlarmLevel());
            }
        }
    }
    
    private String getTemplateByLevel(String level) {
        // 简化实现：使用固定模板
        switch (level.toLowerCase()) {
            case "high":
                return "【物联网系统】紧急报警：设备{deviceName}发生{alarmType}，请立即处理！";
            case "medium":
                return "【物联网系统】重要报警：设备{deviceName}发生{alarmType}，请及时处理。";
            case "low":
                return "【物联网系统】一般报警：设备{deviceName}发生{alarmType}，请关注。";
            default:
                return "【物联网系统】报警：设备{deviceName}发生{alarmType}，请处理。";
        }
    }
    
    private String createMessageFromTemplate(String template, Alarm alarm) {
        return template
            .replace("{deviceName}", alarm.getDeviceName() != null ? alarm.getDeviceName() : "未知设备")
            .replace("{alarmType}", alarm.getAlarmType() != null ? alarm.getAlarmType() : "未知类型");
    }
    
    private Map<String, Object> createTemplateVariables(Alarm alarm) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("deviceName", alarm.getDeviceName());
        variables.put("alarmType", alarm.getAlarmType());
        variables.put("level", alarm.getAlarmLevel());
        variables.put("message", alarm.getMessage());
        variables.put("timestamp", alarm.getCreatedAt());
        return variables;
    }
}