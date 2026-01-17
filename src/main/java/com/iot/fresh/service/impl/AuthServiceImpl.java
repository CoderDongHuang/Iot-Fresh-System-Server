package com.iot.fresh.service.impl;

import com.iot.fresh.dto.LoginRequest;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.UserDto;
import com.iot.fresh.entity.User;
import com.iot.fresh.repository.UserRepository;
import com.iot.fresh.service.AuthService;
import com.iot.fresh.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ApiResponse<Map<String, String>> login(LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                Map<String, String> tokenData = new HashMap<>();
                tokenData.put("token", token);
                return ApiResponse.success("登录成功", tokenData);
            }
        }
        
        return ApiResponse.error("用户名或密码错误");
    }

    @Override
    public ApiResponse<String> logout(String token) {
        // 在实际应用中，可以将token加入黑名单
        return ApiResponse.success("登出成功", "OK");
    }

    @Override
    public ApiResponse<UserDto> getCurrentUser(String token) {
        // 移除 "Bearer " 前缀（如果存在）
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = jwtUtil.getUsernameFromTokenSafely(token);
        
        if (username == null || username.isEmpty()) {
            return ApiResponse.error("无效的token");
        }
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            userDto.setRealName(user.getRealName());
            userDto.setEmail(user.getEmail());
            userDto.setPhone(user.getPhone());
            userDto.setDepartment(user.getDepartment());
            userDto.setPosition(user.getPosition());
            userDto.setAvatar(user.getAvatar());
            userDto.setRole(user.getRole());
            // 设置角色列表，将单个角色转换为列表
            userDto.setRoles(List.of(user.getRole()));
            userDto.setStatus(user.getStatus());
            
            return ApiResponse.success(userDto);
        }
        
        return ApiResponse.error("用户不存在");
    }
}