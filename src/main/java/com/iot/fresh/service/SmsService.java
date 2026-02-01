package com.iot.fresh.service;

public interface SmsService {
    
    /**
     * 发送短信
     * @param phoneNumber 手机号
     * @param message 短信内容
     * @param templateType 模板类型
     * @return 发送是否成功
     */
    boolean sendSms(String phoneNumber, String message, String templateType);
    
    /**
     * 发送模板短信
     * @param phoneNumber 手机号
     * @param templateId 模板ID
     * @param templateParams 模板参数
     * @return 发送是否成功
     */
    boolean sendTemplateSms(String phoneNumber, String templateId, String[] templateParams);
}