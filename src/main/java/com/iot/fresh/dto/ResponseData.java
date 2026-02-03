package com.iot.fresh.dto;

import lombok.Data;

@Data
public class ResponseData<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> ResponseData<T> success() {
        ResponseData<T> response = new ResponseData<>();
        response.setSuccess(true);
        response.setMessage("操作成功");
        return response;
    }
    
    public static <T> ResponseData<T> success(T data) {
        ResponseData<T> response = new ResponseData<>();
        response.setSuccess(true);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }
    
    public static <T> ResponseData<T> error(String message) {
        ResponseData<T> response = new ResponseData<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}