package com.iot.fresh.dto;

import java.time.LocalDateTime;

public class DeviceDto {
    
    private Long id;
    private String vid;
    private String deviceName;
    private String deviceType;
    private Integer status;
    private String location;
    private String contactPhone;
    private String description;
    private String manufacturer;
    private String model;
    private String firmwareVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastHeartbeat;
    
    // 默认构造函数
    public DeviceDto() {}
    
    // 构造函数
    public DeviceDto(String vid, String deviceName, String deviceType, String location, 
                     Integer status, String description) {
        this.vid = vid;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.location = location;
        this.status = status != null ? status : 1;
        this.description = description;
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getVid() { return vid; }
    public void setVid(String vid) { this.vid = vid; }
    
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    
    // 兼容性方法 - 项目中其他代码使用的方法
    public void setLastOnlineTime(LocalDateTime lastOnlineTime) {
        this.lastHeartbeat = lastOnlineTime;
    }
    
    public void setLastOnline_time(LocalDateTime lastOnlineTime) {
        this.lastHeartbeat = lastOnlineTime;
    }
    
    public void setLast_heartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createdAt = createTime;
    }
    
    public void setCreate_time(LocalDateTime createTime) {
        this.createdAt = createTime;
    }
    
    public void setCurrentData(Object currentData) {
        // 兼容方法，不做实际处理
    }
    
    // 前端期望的字段名兼容方法
    public String getLastOnlineTime() {
        return lastHeartbeat != null ? lastHeartbeat.toString() : null;
    }
    
    public String getCreateTime() {
        return createdAt != null ? createdAt.toString() : null;
    }
    
    public String getStatusText() {
        if (status == null) return "unknown";
        switch (status) {
            case 1: return "online";
            case 0: return "offline";
            case 2: return "error";
            case 3: return "maintenance";
            default: return "unknown";
        }
    }
}