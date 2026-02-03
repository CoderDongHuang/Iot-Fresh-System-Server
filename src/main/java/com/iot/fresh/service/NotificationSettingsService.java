package com.iot.fresh.service;

import com.iot.fresh.dto.NotificationSettings;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationSettingsService {
    
    // 存储用户通知设置的模拟存储（实际应该使用数据库）
    private Map<Long, NotificationSettings> userSettings = new HashMap<>();
    
    // 存储短信模板
    private Map<String, String> smsTemplates = new HashMap<>();
    
    // 存储邮件模板
    private Map<String, String> emailTemplates = new HashMap<>();
    
    public NotificationSettingsService() {
        // 初始化默认短信模板
        smsTemplates.put("high", "【物联网系统】紧急报警：设备{deviceName}发生{alarmType}，请立即处理！");
        smsTemplates.put("medium", "【物联网系统】重要报警：设备{deviceName}发生{alarmType}，请及时处理。");
        smsTemplates.put("low", "【物联网系统】一般报警：设备{deviceName}发生{alarmType}，请关注。");
        
        // 初始化默认邮件模板
        emailTemplates.put("high", "【物联网系统】紧急报警：设备{deviceName}发生{alarmType}，请立即处理！");
        emailTemplates.put("medium", "【物联网系统】重要报警：设备{deviceName}发生{alarmType}，请及时处理。");
        emailTemplates.put("low", "【物联网系统】一般报警：设备{deviceName}发生{alarmType}，请关注。");
        
        // 初始化默认设置
        NotificationSettings defaultSettings = new NotificationSettings();
        defaultSettings.setSmsEnabled(true);
        defaultSettings.setPhoneNumbers("13800138000,13900139000");
        defaultSettings.setSoundEnabled(true);
        defaultSettings.setVibrationEnabled(true);
        defaultSettings.setPopupEnabled(true);
        defaultSettings.setNotifyLevels(Arrays.asList("high", "medium"));
        defaultSettings.setPushFrequency("immediate");
        defaultSettings.setQuietHours(new String[]{"22:00", "07:00"});
        
        // 为默认用户设置默认配置
        userSettings.put(1L, defaultSettings);
    }
    
    public NotificationSettings getUserSettings(Long userId) {
        return userSettings.getOrDefault(userId, getDefaultSettings());
    }
    
    public void saveUserSettings(Long userId, NotificationSettings settings) {
        userSettings.put(userId, settings);
    }
    
    public NotificationSettings getDefaultSettings() {
        NotificationSettings defaultSettings = new NotificationSettings();
        defaultSettings.setSmsEnabled(true);
        defaultSettings.setPhoneNumbers("13800138000");
        defaultSettings.setSoundEnabled(true);
        defaultSettings.setVibrationEnabled(true);
        defaultSettings.setPopupEnabled(true);
        defaultSettings.setNotifyLevels(Arrays.asList("high", "medium"));
        defaultSettings.setPushFrequency("immediate");
        defaultSettings.setQuietHours(new String[]{"22:00", "07:00"});
        return defaultSettings;
    }
    
    public Map<String, String> getSmsTemplates() {
        return new HashMap<>(smsTemplates);
    }
    
    public void saveSmsTemplates(Map<String, String> templates) {
        smsTemplates.clear();
        smsTemplates.putAll(templates);
    }
    
    public String getSmsTemplate(String level) {
        return smsTemplates.getOrDefault(level, smsTemplates.get("medium"));
    }
    
    public Map<String, String> getEmailTemplates() {
        return new HashMap<>(emailTemplates);
    }
    
    public void saveEmailTemplates(Map<String, String> templates) {
        emailTemplates.clear();
        emailTemplates.putAll(templates);
    }
    
    public String getEmailTemplate(String level) {
        return emailTemplates.getOrDefault(level, emailTemplates.get("medium"));
    }
}