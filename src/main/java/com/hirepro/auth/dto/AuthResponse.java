package com.hirepro.auth.dto;

public class AuthResponse {
    private String message;
    private String email;
    private String role;
    private long expiresIn;

    // Add these fields back for internal use
    private String accessToken;
    private String refreshToken;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, String email, String role, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
        this.message = "Authentication successful";
    }

    public AuthResponse(String message, String email, String role, long expiresIn) {
        this.message = message;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    // Logout constructor
    public static AuthResponse logoutResponse() {
        return new AuthResponse("Logged out successfully", null, null, 0);
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}