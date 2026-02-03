package com.iot.fresh.service.impl;

import com.iot.fresh.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${email.enabled:false}")
    private boolean emailEnabled;
    
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Override
    public boolean sendAlertEmail(String email, String subject, String content) {
        try {
            if (!emailEnabled) {
                log.info("邮件通知未启用，模拟发送报警邮件到: {}, 主题: {}, 内容: {}", email, subject, content);
                return true;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("报警邮件发送成功: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("发送报警邮件失败: {}, 错误: {}", email, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sendTestEmail(String email) {
        try {
            if (!emailEnabled) {
                log.info("邮件通知未启用，模拟发送测试邮件到: {}", email);
                return true;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【物联网生鲜品储运系统】测试邮件");
            message.setText("这是一封测试邮件，系统运行正常。\n\n系统时间: " + java.time.LocalDateTime.now());
            
            mailSender.send(message);
            log.info("测试邮件发送成功: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("发送测试邮件失败: {}, 错误: {}", email, e.getMessage());
            return false;
        }
    }
}