package com.hirepro.auth.service;

import com.hirepro.auth.dto.AuthResponse;
import com.hirepro.auth.dto.LoginRequest;
import com.hirepro.auth.dto.RegisterRequest;
import com.hirepro.users.dto.UserResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    AuthResponse refreshToken(String refreshToken);

    UserResponse getCurrentUser(String username);
}