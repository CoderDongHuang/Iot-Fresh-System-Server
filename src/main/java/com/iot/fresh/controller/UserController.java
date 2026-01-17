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
            // 确保只能更新自己的信息
            if (!username.equals(userDto.getUsername())) {
                return ApiResponse.error("无权更新他人信息");
            }
            
            boolean success = userService.updateUser(userDto);
            if (success) {
                return ApiResponse.success("用户信息更新成功");
            } else {
                return ApiResponse.error("用户信息更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像接口
     * 
     * 路径: POST /api/user/avatar 和 POST /api/user/upload-avatar
     * 
     * 请求: multipart/form-data 格式上传文件
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "头像上传成功",
     *   "data": {
     *     "avatarUrl": "https://example.com/new-avatar.jpg"
     *   }
     * }
     * 
     * @param request HTTP请求对象
     * @param file 上传的头像文件
     * @return ApiResponse<Map<String, Object>> 包含头像URL的响应对象
     * @author donghuang
     * @since 2026
     */
    @PostMapping("/avatar")
    public ApiResponse<Map<String, Object>> uploadAvatar(HttpServletRequest request, @RequestParam("avatar") MultipartFile file) {
        try {
            String token = jwtUtil.getTokenFromRequest(request);
            if (token == null) {
                return ApiResponse.error("未提供认证令牌");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            System.out.println("正在为用户 " + username + " 上传头像");
            
            // 验证文件是否存在
            if (file.isEmpty()) {
                return ApiResponse.error("请选择要上传的文件");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            System.out.println("上传文件类型: " + contentType);
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("只支持图片格式文件");
            }
            
            // 限制文件大小 (例如最大5MB)
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return ApiResponse.error("文件大小不能超过5MB");
            }
            
            // 创建上传目录
            String uploadDir = System.getProperty("user.home") + "/uploads/avatars/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = username + "_" + System.currentTimeMillis() + "_" + 
                                   Math.abs(UUID.randomUUID().toString().hashCode() % 10000) + fileExtension;
            
            // 保存文件
            Path filePath = Paths.get(uploadDir + uniqueFilename);
            Files.write(filePath, file.getBytes());
            
            // 构建返回的头像URL（相对于应用根路径）
            String avatarUrl = "/uploads/avatars/" + uniqueFilename;
            System.out.println("头像URL: " + avatarUrl);
            
            // 更新用户的头像信息到数据库
            UserDto userDto = userService.getUserByUsername(username);
            if (userDto != null) {
                System.out.println("原始头像URL: " + userDto.getAvatar());
                userDto.setAvatar(avatarUrl);
                boolean updated = userService.updateUser(userDto);
                System.out.println("数据库更新结果: " + updated);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("filename", uniqueFilename);
            result.put("size", file.getSize());
            
            System.out.println("头像上传成功: " + avatarUrl);
            return ApiResponse.success("头像上传成功", result);
        } catch (IOException e) {
            System.out.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("头像上传失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像接口（兼容路径）
     * 
     * 路径: POST /api/user/upload-avatar
     * 
     * 请求: multipart/form-data 格式上传文件
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "msg": "头像上传成功",
     *   "data": {
     *     "avatarUrl": "https://example.com/new-avatar.jpg"
     *   }
     * }
     * 
     * @param request HTTP请求对象
     * @param file 上传的头像文件
     * @return ApiResponse<Map<String, Object>> 包含头像URL的响应对象
     * @author donghuang
     * @since 2026
     */
    @PostMapping("/upload-avatar")
    public ApiResponse<Map<String, Object>> uploadAvatarCompatible(HttpServletRequest request, @RequestParam("avatar") MultipartFile file) {
        return uploadAvatar(request, file);
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