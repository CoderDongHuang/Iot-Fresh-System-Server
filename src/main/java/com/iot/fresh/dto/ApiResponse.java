package com.iot.fresh.dto;

import java.util.List;

/**
 * 统一API响应格式
 */
public class ApiResponse<T> {
    
    private int code;
    private String message;
    private T data;
    private boolean success;
    
    // 默认构造函数
    public ApiResponse() {}
    
    // 完整构造函数
    public ApiResponse(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null, true);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null, true);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, true);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, true);
    }
    
    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null, false);
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, false);
    }
    
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, false);
    }
    
    /**
     * 分页数据成功响应
     */
    public static <T> ApiResponse<PaginatedResponse<T>> paginatedSuccess(List<T> list, long total) {
        PaginatedResponse<T> paginatedData = new PaginatedResponse<T>(list, total);
        return new ApiResponse<>(200, "操作成功", paginatedData, true);
    }
    
    public static <T> ApiResponse<PaginatedResponse<T>> paginatedSuccess(List<T> list, long total, String message) {
        PaginatedResponse<T> paginatedData = new PaginatedResponse<T>(list, total);
        return new ApiResponse<>(200, message, paginatedData, true);
    }
    
    // Getter和Setter方法
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    // 兼容性方法 - 项目中其他代码使用的方法
    public String getMsg() { return message; }
    public void setMsg(String msg) { this.message = msg; }
    
    public T getList() { return data; }
    public void setList(T list) { this.data = list; }
    
    public T getTotal() { return data; }
    public void setTotal(T total) { this.data = total; }
}