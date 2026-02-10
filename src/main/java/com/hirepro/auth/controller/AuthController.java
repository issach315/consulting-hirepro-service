package com.hirepro.auth.controller;

import com.hirepro.auth.dto.AuthResponse;
import com.hirepro.auth.dto.LoginRequest;
import com.hirepro.auth.dto.RefreshTokenRequest;
import com.hirepro.auth.dto.RegisterRequest;
import com.hirepro.auth.service.AuthService;
import com.hirepro.common.response.ApiResponse;
import com.hirepro.users.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        ApiResponse<AuthResponse> response = ApiResponse.success("Login successful", authResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        ApiResponse<UserResponse> response = ApiResponse.success("User registered successfully", userResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
        ApiResponse<AuthResponse> response = ApiResponse.success("Token refreshed successfully", authResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserResponse userResponse = authService.getCurrentUser(username);
        ApiResponse<UserResponse> response = ApiResponse.success("User details retrieved successfully", userResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}