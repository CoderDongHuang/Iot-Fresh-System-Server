package com.iot.fresh.service;

import com.iot.fresh.dto.LoginRequest;
import com.iot.fresh.dto.ApiResponse;
import com.iot.fresh.dto.UserDto;

public interface AuthService {
    ApiResponse<String> login(LoginRequest loginRequest);
    ApiResponse<String> logout(String token);
    ApiResponse<UserDto> getCurrentUser(String token);
}