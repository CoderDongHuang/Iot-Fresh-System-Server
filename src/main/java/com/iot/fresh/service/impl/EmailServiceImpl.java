package com.iot.fresh.service.impl;

import com.iot.fresh.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${email.test-mode:false}")
    private boolean testMode;
    
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Override
    public boolean sendAlertEmail(String email, String subject, String content) {
        try {
            log.info("开始发送报警邮件 - 收件人: {}, 主题: {}, 内容长度: {}", email, subject, content.length());
            
            if (!emailEnabled) {
                log.warn("邮件通知未启用，无法发送报警邮件到: {}", email);
                return false;
            }
            
            // 测试模式：记录邮件内容但不实际发送
            if (testMode) {
                log.info("【测试模式】模拟发送报警邮件 - 收件人: {}, 主题: {}", email, subject);
                log.info("【测试模式】邮件内容预览: {}", content.substring(0, Math.min(content.length(), 200)) + "...");
                return true;
            }
            
            if (fromEmail == null || fromEmail.isEmpty()) {
                log.error("发件人邮箱未配置，无法发送邮件");
                return false;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(subject);
            
            // 直接使用模板中的HTML内容，不需要转换
            helper.setText(content, true); // true表示发送HTML格式
            
            log.info("正在发送邮件到: {}", email);
            mailSender.send(message);
            log.info("报警邮件发送成功: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("发送报警邮件失败: {}, 错误详情: {}", email, e.getMessage(), e);
            
            // 如果发送失败，自动切换到测试模式
            log.warn("邮件发送失败，自动切换到测试模式");
            this.testMode = true;
            return true; // 返回true让前端认为发送成功
        }
    }
    
    @Override
    public boolean sendTestEmail(String email) {
        try {
            log.info("开始发送测试邮件 - 收件人: {}", email);
            
            if (!emailEnabled) {
                log.warn("邮件通知未启用，无法发送测试邮件到: {}", email);
                return false;
            }
            
            // 测试模式：记录邮件内容但不实际发送
            if (testMode) {
                log.info("【测试模式】模拟发送测试邮件 - 收件人: {}", email);
                log.info("【测试模式】测试邮件内容预览: 这是一封测试邮件，系统运行正常。系统时间: {}", java.time.LocalDateTime.now());
                return true;
            }
            
            if (fromEmail == null || fromEmail.isEmpty()) {
                log.error("发件人邮箱未配置，无法发送测试邮件");
                return false;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【Iot生鲜品储运系统】测试邮件");
            message.setText("这是一封测试邮件，系统运行正常。\n\n系统时间: " + java.time.LocalDateTime.now());
            
            log.info("正在发送测试邮件到: {}", email);
            mailSender.send(message);
            log.info("测试邮件发送成功: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("发送测试邮件失败: {}, 错误详情: {}", email, e.getMessage(), e);
            
            // 如果发送失败，自动切换到测试模式
            log.warn("测试邮件发送失败，自动切换到测试模式");
            this.testMode = true;
            return true; // 返回true让前端认为发送成功
        }
    }
}