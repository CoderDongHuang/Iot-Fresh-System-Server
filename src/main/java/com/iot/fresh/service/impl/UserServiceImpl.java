package com.iot.fresh.service.impl;

import com.iot.fresh.dto.UserDto;
import com.iot.fresh.entity.User;
import com.iot.fresh.repository.UserRepository;
import com.iot.fresh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户服务实现类
 * 实现用户信息管理相关业务方法
 * 
 * @author donghuang
 * @since 2026
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDto getUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserDto userDto = new UserDto();
            userDto.setUsername(user.getUsername());
            userDto.setRealName(user.getRealName());
            userDto.setEmail(user.getEmail());
            userDto.setPhone(user.getPhone());
            userDto.setDepartment(user.getDepartment());
            userDto.setPosition(user.getPosition());
            userDto.setAvatar(user.getAvatar());
            return userDto;
        }
        return null;
    }

    @Override
    public boolean updateUser(UserDto userDto) {
        Optional<User> userOptional = userRepository.findByUsername(userDto.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 记录更新前的信息
            System.out.println("更新前 - 用户: " + user.getUsername() + ", 头像: " + user.getAvatar());
            
            user.setRealName(userDto.getRealName());
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setDepartment(userDto.getDepartment());
            user.setPosition(userDto.getPosition());
            
            // 头像处理：直接使用前端发送的头像URL
            if (userDto.getAvatar() != null && userDto.getAvatar().trim().length() > 0) {
                // 验证头像URL格式
                if (isValidAvatarUrl(userDto.getAvatar())) {
                    user.setAvatar(userDto.getAvatar());
                    System.out.println("更新头像URL: " + userDto.getAvatar());
                } else {
                    // 如果头像URL无效，使用默认头像
                    String defaultAvatar = "https://cube.elemecdn.com/0/88/03b0d39583f4c99b3a30486abba70jpeg.jpeg";
                    user.setAvatar(defaultAvatar);
                    System.out.println("头像URL无效，使用默认头像: " + defaultAvatar);
                }
            } else {
                // 如果头像为空，使用默认头像
                String defaultAvatar = "https://cube.elemecdn.com/0/88/03b0d39583f4c99b3a30486abba70jpeg.jpeg";
                user.setAvatar(defaultAvatar);
                System.out.println("头像为空，使用默认头像: " + defaultAvatar);
            }
            
            // 强制更新更新时间戳，确保Hibernate检测到实体变化
            user.setUpdatedAt(java.time.LocalDateTime.now());
            
            try {
                userRepository.save(user);
                System.out.println("用户信息更新成功 - 用户: " + user.getUsername() + ", 新头像: " + user.getAvatar());
                return true;
            } catch (Exception e) {
                System.out.println("用户信息更新失败: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("未找到用户: " + userDto.getUsername());
        }
        return false;
    }
    
    /**
     * 验证头像URL是否有效
     * @param avatarUrl 头像URL
     * @return 是否有效
     */
    private boolean isValidAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return false;
        }
        
        // 简单的URL格式验证
        return avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://") || 
               avatarUrl.startsWith("//") || avatarUrl.startsWith("/");
    }
    


    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 验证旧密码
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                // 加密新密码并更新
                String encodedNewPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encodedNewPassword);
                
                try {
                    userRepository.save(user);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                // 旧密码不匹配
                return false;
            }
        }
        return false;
    }
}