package com.iot.fresh.service;

public interface EmailService {
    
    /**
     * 发送报警邮件
     * @param email 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送是否成功
     */
    boolean sendAlertEmail(String email, String subject, String content);
    
    /**
     * 发送测试邮件
     * @param email 收件人邮箱
     * @return 发送是否成功
     */
    boolean sendTestEmail(String email);
}