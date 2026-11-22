package com.smartcampus.service;

import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.request.RefreshTokenRequest;
import com.smartcampus.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
}
