package com.iot.fresh.controller;

import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.UserDto;
import com.iot.fresh.entity.User;
import com.iot.fresh.service.UserService;
import com.iot.fresh.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 用户控制器
 * 提供用户信息管理和密码修改相关API接口
 * 
 * @author donghuang
 * @since 2026
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户信息接口
     * 
     * 路径: GET /api/user/profile
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "username": "admin",
     *     "realName": "管理员",
     *     "email": "admin@example.com",
     *     "phone": "13800138000",
     *     "department": "tech",
     *     "position": "系统管理员",
     *     "avatar": "https://example.com/avatar.jpg"
     *   }
     * }
     * 
     * @param request HTTP请求对象
     * @return ApiResponse<UserDto> 包含用户信息的响应对象
     * @author donghuang
     * @since 2026
     */
    @GetMapping("/profile")
    public ApiResponse<UserDto> getUserProfile(HttpServletRequest request) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token == null) {
                return ApiResponse.error("未提供认证令牌");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            UserDto userDto = userService.getUserByUsername(username);
            
            if (userDto != null) {
                return ApiResponse.success(userDto);
            } else {
                return ApiResponse.error("用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息接口
     * 
     * 路径: PUT /api/user/profile
     * 
     * 请求体:
     * {
     *   "username": "admin",
     *   "realName": "管理员",
     *   "email": "admin@example.com",
     *   "phone": "13800138000",
     *   "department": "tech",
     *   "position": "系统管理员"
     * }
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "用户信息更新成功"
     * }
     * 
     * @param request HTTP请求对象
     * @param userDto 用户信息数据传输对象
     * @return ApiResponse<String> 更新结果响应对象
     * @author donghuang
     * @since 2026
     */
    @PutMapping("/profile")
    public ApiResponse<String> updateUserProfile(HttpServletRequest request, @RequestBody UserDto userDto) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token == null) {
                return ApiResponse.error("未提供认证令牌");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            System.out.println("=== 接收到更新用户资料请求 ===");
            System.out.println("当前登录用户: " + username);
            System.out.println("请求中的用户名: " + userDto.getUsername());
            System.out.println("请求中的真实姓名: " + userDto.getRealName());
            System.out.println("请求中的邮箱: " + userDto.getEmail());
            System.out.println("请求中的电话: " + userDto.getPhone());
            System.out.println("请求中的头像URL: " + userDto.getAvatar());
            
            // 确保只能更新自己的信息
            if (!username.equals(userDto.getUsername())) {
                return ApiResponse.error("无权更新他人信息");
            }
            
            boolean success = userService.updateUser(userDto);
            if (success) {
                System.out.println("用户信息更新成功: " + username);
                return ApiResponse.success("用户信息更新成功");
            } else {
                System.out.println("用户信息更新失败: " + username);
                return ApiResponse.error("用户信息更新失败");
            }
        } catch (Exception e) {
            System.out.println("更新用户信息异常: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("更新用户信息失败: " + e.getMessage());
        }
    }





    /**
     * 修改密码接口
     * 
     * 路径: PUT /api/user/password
     * 
     * 请求体:
     * {
     *   "oldPassword": "旧密码",
     *   "newPassword": "新密码"
     * }
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "密码修改成功"
     * }
     * 
     * 错误响应:
     * {
     *   "code": 400,
     *   "msg": "原密码错误"
     * }
     * 
     * @param request HTTP请求对象
     * @param passwordChangeMap 密码修改请求参数
     * @return ApiResponse<String> 密码修改结果响应对象
     * @author donghuang
     * @since 2026
     */
    @PutMapping("/password")
    public ApiResponse<String> changePassword(HttpServletRequest request, @RequestBody Map<String, String> passwordChangeMap) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token == null) {
                return ApiResponse.error("未提供认证令牌");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            String oldPassword = passwordChangeMap.get("oldPassword");
            String newPassword = passwordChangeMap.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ApiResponse.error("密码不能为空");
            }
            
            if (newPassword.length() < 6) {
                return ApiResponse.error("新密码长度不能少于6位");
            }
            
            boolean success = userService.changePassword(username, oldPassword, newPassword);
            if (success) {
                return ApiResponse.success("密码修改成功");
            } else {
                return ApiResponse.error("原密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("密码修改失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息接口
     * 
     * 路径: GET /api/user/info
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "username": "admin",
     *     "realName": "管理员",
     *     "email": "admin@example.com",
     *     "phone": "13800138000",
     *     "department": "tech",
     *     "position": "系统管理员",
     *     "avatar": "https://example.com/avatar.jpg"
     *   }
     * }
     * 
     * @param request HTTP请求对象
     * @return ApiResponse<UserDto> 包含用户信息的响应对象
     * @author donghuang
     * @since 2026
     */
    @GetMapping("/info")
    public ApiResponse<UserDto> getCurrentUserInfo(HttpServletRequest request) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token == null) {
                return ApiResponse.error("未提供认证令牌");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            UserDto userDto = userService.getUserByUsername(username);
            
            if (userDto != null) {
                return ApiResponse.success(userDto);
            } else {
                return ApiResponse.error("用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }
}