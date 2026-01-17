package com.iot.fresh.service.impl;

import com.iot.fresh.dto.UserDto;
import com.iot.fresh.entity.User;
import com.iot.fresh.repository.UserRepository;
import com.iot.fresh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            user.setRealName(userDto.getRealName());
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setDepartment(userDto.getDepartment());
            user.setPosition(userDto.getPosition());
            
            // 更新头像：如果提供了头像，则更新；否则保持原头像不变
            if (userDto.getAvatar() != null) {
                user.setAvatar(userDto.getAvatar());
            }
            
            // 强制更新更新时间戳，确保Hibernate检测到实体变化
            user.setUpdatedAt(java.time.LocalDateTime.now());
            
            try {
                userRepository.save(user);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
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