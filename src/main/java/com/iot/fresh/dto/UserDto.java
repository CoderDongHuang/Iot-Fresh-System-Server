package com.iot.fresh.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;  // 单个角色
    private List<String> roles;  // 角色列表
    private Integer status;
}