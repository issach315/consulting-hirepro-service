package com.hirepro.auth.service;

import com.hirepro.auth.dto.AuthResponse;
import com.hirepro.auth.dto.LoginRequest;
import com.hirepro.auth.dto.RegisterRequest;
import com.hirepro.users.dto.AuthUserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthUserResponse register(RegisterRequest request);
    AuthResponse refreshToken(String refreshToken);
    AuthUserResponse getCurrentUser(String username);
}