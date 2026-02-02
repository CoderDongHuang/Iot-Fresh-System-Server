package com.iot.fresh.service.impl;

import com.iot.fresh.service.SmsService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TencentSmsServiceImpl implements SmsService {
    
    @Value("${sms.access-key}")
    private String secretId;
    
    @Value("${sms.secret-key}")
    private String secretKey;
    
    @Value("${sms.sign-name}")
    private String signName;
    
    @Value("${sms.template-codes.high}")
    private String highTemplateId;
    
    @Value("${sms.template-codes.medium}")
    private String mediumTemplateId;
    
    @Value("${sms.template-codes.low}")
    private String lowTemplateId;
    
    @Override
    public boolean sendSms(String phoneNumber, String message, String templateType) {
        try {
            Credential cred = new Credential(secretId, secretKey);
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId("1400009099"); // 默认应用ID，实际使用时请替换
            req.setSignName(signName);
            req.setTemplateId(getTemplateIdByType(templateType));
            req.setPhoneNumberSet(new String[]{phoneNumber});
            req.setTemplateParamSet(new String[]{message});
            
            SendSmsResponse resp = client.SendSms(req);
            
            if (resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                String code = resp.getSendStatusSet()[0].getCode();
                boolean success = "Ok".equals(code);
                
                if (success) {
                    log.info("短信发送成功 - 手机号: {}, 模板类型: {}", phoneNumber, templateType);
                } else {
                    log.error("短信发送失败 - 手机号: {}, 错误码: {}", phoneNumber, code);
                }
                
                return success;
            }
            
            log.error("短信发送失败 - 手机号: {}, 响应为空", phoneNumber);
            return false;
            
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云短信发送失败 - 手机号: {}, 错误: {}", phoneNumber, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("短信发送异常 - 手机号: {}, 错误: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sendTemplateSms(String phoneNumber, String templateId, String[] templateParams) {
        try {
            Credential cred = new Credential(secretId, secretKey);
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId("1400009099"); // 默认应用ID，实际使用时请替换
            req.setSignName(signName);
            req.setTemplateId(templateId);
            req.setPhoneNumberSet(new String[]{phoneNumber});
            req.setTemplateParamSet(templateParams);
            
            SendSmsResponse resp = client.SendSms(req);
            
            if (resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                String code = resp.getSendStatusSet()[0].getCode();
                boolean success = "Ok".equals(code);
                
                if (success) {
                    log.info("模板短信发送成功 - 手机号: {}, 模板ID: {}", phoneNumber, templateId);
                } else {
                    log.error("模板短信发送失败 - 手机号: {}, 错误码: {}", phoneNumber, code);
                }
                
                return success;
            }
            
            log.error("模板短信发送失败 - 手机号: {}, 响应为空", phoneNumber);
            return false;
            
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云模板短信发送失败 - 手机号: {}, 错误: {}", phoneNumber, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("模板短信发送异常 - 手机号: {}, 错误: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    private String getTemplateIdByType(String templateType) {
        switch (templateType.toLowerCase()) {
            case "high":
                return highTemplateId;
            case "medium":
                return mediumTemplateId;
            case "low":
                return lowTemplateId;
            default:
                return highTemplateId; // 默认使用高优先级模板
        }
    }
}