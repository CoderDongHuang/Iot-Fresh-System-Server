package com.iot.fresh.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EmailNotifyRequest {
    private List<String> emailAddresses;
    private String subject;
    private String content;
    private Map<String, Object> variables;
}