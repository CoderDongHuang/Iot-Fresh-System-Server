package com.iot.fresh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class SmsService {
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${sms.provider:aliyun}")
    private String smsProvider;
    
    @Value("${sms.access-key:}")
    private String accessKey;
    
    @Value("${sms.secret-key:}")
    private String secretKey;
    
    @Value("${sms.sign-name:}")
    private String signName;
    
    @Value("${sms.template-codes.high:SMS_001}")
    private String highTemplateCode;
    
    @Value("${sms.template-codes.medium:SMS_002}")
    private String mediumTemplateCode;
    
    @Value("${sms.template-codes.low:SMS_003}")
    private String lowTemplateCode;
    
    public void sendTemplateSms(List<String> phoneNumbers, String template, Map<String, Object> variables) {
        if (!smsEnabled) {
            System.out.println("短信服务未启用，跳过发送");
            return;
        }
        
        // 阿里云短信服务实现
        if ("aliyun".equals(smsProvider)) {
            sendAliyunSms(phoneNumbers, template, variables);
        } 
        // 腾讯云短信服务实现
        else if ("tencent".equals(smsProvider)) {
            sendTencentSms(phoneNumbers, template, variables);
        }
        else {
            // 模拟发送，用于开发和测试
            sendMockSms(phoneNumbers, template, variables);
        }
    }
    
    private void sendAliyunSms(List<String> phoneNumbers, String template, Map<String, Object> variables) {
        // 这里实现阿里云短信发送逻辑
        // 实际项目中需要集成阿里云SDK
        System.out.println("发送阿里云短信:");
        System.out.println("手机号: " + phoneNumbers);
        System.out.println("模板: " + template);
        System.out.println("变量: " + variables);
        
        // 模拟发送成功
        System.out.println("短信发送成功（模拟）");
    }
    
    private void sendTencentSms(List<String> phoneNumbers, String template, Map<String, Object> variables) {
        // 腾讯云短信服务实现
        // 实际项目中需要集成腾讯云SDK
        System.out.println("发送腾讯云短信:");
        System.out.println("手机号: " + phoneNumbers);
        System.out.println("模板: " + template);
        System.out.println("变量: " + variables);
        
        // 模拟发送成功
        System.out.println("腾讯云短信发送成功（模拟）");
    }
    
    private void sendMockSms(List<String> phoneNumbers, String template, Map<String, Object> variables) {
        // 模拟短信发送，用于开发和测试
        System.out.println("模拟发送短信:");
        System.out.println("手机号: " + phoneNumbers);
        System.out.println("模板: " + template);
        System.out.println("变量: " + variables);
        
        // 构建短信内容
        String content = buildSmsContent(template, variables);
        System.out.println("短信内容: " + content);
        System.out.println("短信发送成功（模拟）");
    }
    
    public void sendTestSms(String phoneNumber, String message) {
        System.out.println("发送测试短信:");
        System.out.println("手机号: " + phoneNumber);
        System.out.println("内容: " + message);
        System.out.println("测试短信发送成功（模拟）");
    }
    
    private String buildSmsContent(String template, Map<String, Object> variables) {
        String content = template;
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                content = content.replace("{" + entry.getKey() + "}", 
                    entry.getValue() != null ? entry.getValue().toString() : "");
            }
        }
        return content;
    }
    
    // 获取默认的短信模板
    public Map<String, String> getDefaultTemplates() {
        return Map.of(
            "high", "【物联网系统】紧急报警：设备{deviceName}发生{alarmType}，请立即处理！",
            "medium", "【物联网系统】重要报警：设备{deviceName}发生{alarmType}，请及时处理。",
            "low", "【物联网系统】一般报警：设备{deviceName}发生{alarmType}，请关注。"
        );
    }
}