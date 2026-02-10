package com.iot.fresh.dto;

import java.util.List;

/**
 * 分页响应数据格式
 */
public class PaginatedResponse<T> {
    
    private List<T> list;
    private long total;
    
    // 默认构造函数
    public PaginatedResponse() {}
    
    // 完整构造函数
    public PaginatedResponse(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }
    
    // 兼容性构造函数（用于现有代码）
    public PaginatedResponse(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
    }
    
    // Getter和Setter方法
    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}