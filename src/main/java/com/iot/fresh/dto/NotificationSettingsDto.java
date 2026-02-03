package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class NotificationSettingsDto {
    private Boolean enabled = false;
    
    @JsonProperty("emails")
    private List<String> emails = new ArrayList<>();
    
    @JsonProperty("notifyLevels")
    private List<String> notifyLevels = Arrays.asList("high", "medium");
    
    @JsonProperty("quietHours")
    private List<String> quietHours = Arrays.asList("22:00", "07:00");
    
    private String pushFrequency = "immediate";
}