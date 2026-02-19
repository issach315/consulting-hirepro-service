package com.hirepro.auth.controller;

import com.hirepro.auth.dto.AuthResponse;
import com.hirepro.auth.dto.LoginRequest;
import com.hirepro.auth.dto.RefreshTokenRequest;
import com.hirepro.auth.dto.RegisterRequest;
import com.hirepro.auth.service.AuthService;
import com.hirepro.auth.util.CookieUtil;
import com.hirepro.auth.util.JwtUtil;
import com.hirepro.common.response.ApiResponse;
import com.hirepro.users.dto.AuthUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, CookieUtil cookieUtil, JwtUtil jwtUtil) {
        this.authService = authService;
        this.cookieUtil = cookieUtil;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);

        // Set tokens in HTTP-only cookies
        cookieUtil.createAccessTokenCookie(response, authResponse.getAccessToken(),
                jwtUtil.getAccessTokenExpiration() / 1000);
        cookieUtil.createRefreshTokenCookie(response, authResponse.getRefreshToken(),
                jwtUtil.getRefreshTokenExpiration() / 1000);

        // Remove tokens from response body
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.success("Login successful", authResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthUserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthUserResponse userResponse = authService.register(request);
        ApiResponse<AuthUserResponse> response = ApiResponse.success("User registered successfully", userResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in cookies"));

        AuthResponse authResponse = authService.refreshToken(refreshToken);

        // Set new access token in cookie
        cookieUtil.createAccessTokenCookie(response, authResponse.getAccessToken(),
                jwtUtil.getAccessTokenExpiration() / 1000);

        // If refresh token was rotated, set new refresh token cookie
        if (authResponse.getRefreshToken() != null) {
            cookieUtil.createRefreshTokenCookie(response, authResponse.getRefreshToken(),
                    jwtUtil.getRefreshTokenExpiration() / 1000);
        }

        // Remove tokens from response body
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.success("Token refreshed successfully", authResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // Clear cookies
        cookieUtil.clearAccessTokenCookie(response);
        cookieUtil.clearRefreshTokenCookie(response);

        ApiResponse<Void> apiResponse = ApiResponse.success("Logged out successfully", null);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        AuthUserResponse userResponse = authService.getCurrentUser(username);
        ApiResponse<AuthUserResponse> response = ApiResponse.success("User details retrieved successfully", userResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}