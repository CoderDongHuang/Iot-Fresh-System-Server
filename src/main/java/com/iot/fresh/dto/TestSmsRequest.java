package com.iot.fresh.dto;

import lombok.Data;

@Data
public class TestSmsRequest {
    private String phoneNumber;
    private String message;
}