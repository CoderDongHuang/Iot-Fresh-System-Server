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
        
        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getUsername());
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("token", token);
            return ApiResponse.success(tokenData);  // 使用默认消息 "success"
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
            userDto.setEmail(user.getEmail());
            userDto.setPhone(user.getPhone());
            userDto.setRole(user.getRole());
            userDto.setStatus(user.getStatus());
            
            return ApiResponse.success(userDto);
        }
        
        return ApiResponse.error("用户不存在");
    }
}