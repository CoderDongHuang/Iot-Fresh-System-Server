package com.iot.fresh.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationSettings {
    private boolean smsEnabled;
    private String phoneNumbers; // 逗号分隔
    private boolean soundEnabled;
    private boolean vibrationEnabled;
    private boolean popupEnabled;
    private List<String> notifyLevels; // ["high", "medium", "low"]
    private String pushFrequency; // "immediate", "batch", "summary"
    private String[] quietHours; // ["22:00", "07:00"]
}