package com.iot.fresh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_data_history")
public class DeviceDataHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vid", nullable = false, length = 50)
    private String vid;
    
    @Column(name = "tin")
    private Double tin; // 内部温度
    
    @Column(name = "tout")
    private Double tout; // 外部温度
    
    @Column(name = "hin")
    private Integer hin; // 内部湿度
    
    @Column(name = "hout")
    private Integer hout; // 外部湿度
    
    @Column(name = "lxin")
    private Integer lxin; // 内部光照
    
    @Column(name = "lxout")
    private Integer lxout; // 外部光照
    
    @Column(name = "brightness")
    private Integer brightness; // 亮度
    
    @Column(name = "vstatus")
    private Integer vstatus; // 设备状态 -- 0:离线, 1:在线, 2:故障, 3:维护
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public DeviceDataHistory() {}
    
    public DeviceDataHistory(String vid, Double tin, Double tout, Integer hin, Integer hout, 
                           Integer lxin, Integer lxout, Integer brightness, Integer vstatus, 
                           LocalDateTime updatedAt) {
        this.vid = vid;
        this.tin = tin;
        this.tout = tout;
        this.hin = hin;
        this.hout = hout;
        this.lxin = lxin;
        this.lxout = lxout;
        this.brightness = brightness;
        this.vstatus = vstatus;
        this.updatedAt = updatedAt;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getVid() {
        return vid;
    }
    
    public void setVid(String vid) {
        this.vid = vid;
    }
    
    public Double getTin() {
        return tin;
    }
    
    public void setTin(Double tin) {
        this.tin = tin;
    }
    
    public Double getTout() {
        return tout;
    }
    
    public void setTout(Double tout) {
        this.tout = tout;
    }
    
    public Integer getHin() {
        return hin;
    }
    
    public void setHin(Integer hin) {
        this.hin = hin;
    }
    
    public Integer getHout() {
        return hout;
    }
    
    public void setHout(Integer hout) {
        this.hout = hout;
    }
    
    public Integer getLxin() {
        return lxin;
    }
    
    public void setLxin(Integer lxin) {
        this.lxin = lxin;
    }
    
    public Integer getLxout() {
        return lxout;
    }
    
    public void setLxout(Integer lxout) {
        this.lxout = lxout;
    }
    
    public Integer getBrightness() {
        return brightness;
    }
    
    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }
    
    public Integer getVstatus() {
        return vstatus;
    }
    
    public void setVstatus(Integer vstatus) {
        this.vstatus = vstatus;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}