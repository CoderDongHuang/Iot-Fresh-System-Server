package com.iot.fresh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iot.fresh.entity.SmsSettings;
import lombok.Data;
import java.util.Arrays;
import java.util.List;

@Data
public class SmsSettingsDto {
    private Boolean enabled = false;
    
    @JsonProperty("phoneNumbers")
    private List<String> phoneNumbers = Arrays.asList("13800138000");
    
    @JsonProperty("notifyLevels")
    private List<String> notifyLevels = Arrays.asList("high", "medium");
    
    @JsonProperty("quietHours")
    private String[] quietHours = {"22:00", "07:00"};
    
    private String pushFrequency = "immediate";
    
    // 转换为实体类的方法
    public SmsSettings toEntity() {
        SmsSettings entity = new SmsSettings();
        entity.setEnabled(this.enabled);
        
        // 将List转换为JSON字符串
        if (this.phoneNumbers != null) {
            entity.setPhoneNumbers(convertListToJson(this.phoneNumbers));
        }
        
        if (this.notifyLevels != null) {
            entity.setNotifyLevels(convertListToJson(this.notifyLevels));
        }
        
        if (this.quietHours != null) {
            entity.setQuietHours(convertArrayToJson(this.quietHours));
        }
        
        entity.setPushFrequency(this.pushFrequency);
        return entity;
    }
    
    // 从实体类转换的方法
    public static SmsSettingsDto fromEntity(SmsSettings entity) {
        if (entity == null) {
            return new SmsSettingsDto();
        }
        
        SmsSettingsDto dto = new SmsSettingsDto();
        dto.setEnabled(entity.getEnabled());
        dto.setPushFrequency(entity.getPushFrequency());
        
        // 将JSON字符串转换为List
        if (entity.getPhoneNumbers() != null) {
            dto.setPhoneNumbers(convertJsonToList(entity.getPhoneNumbers()));
        }
        
        if (entity.getNotifyLevels() != null) {
            dto.setNotifyLevels(convertJsonToList(entity.getNotifyLevels()));
        }
        
        if (entity.getQuietHours() != null) {
            dto.setQuietHours(convertJsonToArray(entity.getQuietHours()));
        }
        
        return dto;
    }
    
    // 辅助方法：List转JSON字符串
    private static String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
    
    // 辅助方法：数组转JSON字符串
    private static String convertArrayToJson(String[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(array[i]).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
    
    // 辅助方法：JSON字符串转List
    private static List<String> convertJsonToList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Arrays.asList();
        }
        // 简化实现：移除方括号和引号，然后分割
        String cleanJson = json.replaceAll("[\\[\\]\"\"]", "");
        if (cleanJson.isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(cleanJson.split(","));
    }
    
    // 辅助方法：JSON字符串转数组
    private static String[] convertJsonToArray(String json) {
        List<String> list = convertJsonToList(json);
        return list.toArray(new String[0]);
    }
}