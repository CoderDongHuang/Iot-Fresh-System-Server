package com.iot.fresh.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SmsNotifyRequest {
    private List<String> phoneNumbers;
    private String template;
    private Map<String, Object> variables; // 模板变量
}