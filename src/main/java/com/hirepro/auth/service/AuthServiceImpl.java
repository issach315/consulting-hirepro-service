package com.hirepro.auth.service;

import com.hirepro.auth.dto.AuthResponse;
import com.hirepro.auth.dto.LoginRequest;
import com.hirepro.auth.dto.RegisterRequest;
import com.hirepro.auth.entity.RefreshToken;
import com.hirepro.auth.repository.RefreshTokenRepository;
import com.hirepro.auth.util.JwtUtil;
import com.hirepro.common.exception.BadRequestException;
import com.hirepro.common.exception.ResourceNotFoundException;
import com.hirepro.common.exception.UnauthorizedException;
import com.hirepro.common.util.UlidGenerator;
import com.hirepro.users.dto.AuthUserResponse;
import com.hirepro.users.entity.AuthUser;
import com.hirepro.users.repository.AuthUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(AuthUserRepository authUserRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager) {
        this.authUserRepository = authUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            AuthUser user = authUserRepository.findByEmailAndNotDeleted(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check account status
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new UnauthorizedException("Account is not active. Status: " + user.getStatus());
            }

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            authUserRepository.save(user);

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

            // Save refresh token
            saveRefreshToken(user.getId(), refreshToken);

            return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole(),
                    jwtUtil.getAccessTokenExpiration() / 1000);

        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        // Check if email exists
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        AuthUser user = new AuthUser();
        user.setId(UlidGenerator.generate());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().name());
        user.setStatus("ACTIVE");
        user.setCreatedBy("SELF");

        // Set client_id to null for SUPERADMIN
        if ("SUPERADMIN".equals(request.getRole().name())) {
            user.setClientId(null);
            user.setEmployeeType(null);
        }

        AuthUser savedUser = authUserRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token
        RefreshToken token = refreshTokenRepository.findValidToken(refreshToken, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        // Get user
        AuthUser user = authUserRepository.findByIdAndNotDeleted(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check account status
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new UnauthorizedException("Account is not active");
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        // Optionally rotate refresh token for better security
        boolean rotateRefreshToken = true; // This could be configurable
        String newRefreshToken = null;

        if (rotateRefreshToken) {
            // Revoke old token
            token.setRevoked(true);
            refreshTokenRepository.save(token);

            // Generate new refresh token
            newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
            saveRefreshToken(user.getId(), newRefreshToken);
        }

        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), user.getRole(),
                jwtUtil.getAccessTokenExpiration() / 1000);
    }

    @Override
    public AuthUserResponse getCurrentUser(String email) {
        AuthUser user = authUserRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    private void saveRefreshToken(String userId, String token) {
        // Revoke existing tokens for the user
        refreshTokenRepository.revokeAllUserTokens(userId);

        // Create new refresh token
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000);
        RefreshToken refreshToken = new RefreshToken(userId, token, expiresAt);
        refreshTokenRepository.save(refreshToken);
    }

    private AuthUserResponse mapToUserResponse(AuthUser user) {
        AuthUserResponse response = new AuthUserResponse();
        response.setId(user.getId());
        response.setClientId(user.getClientId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setEmployeeType(user.getEmployeeType());
        response.setStatus(user.getStatus());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedBy(user.getCreatedBy());
        response.setUpdatedBy(user.getUpdatedBy());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}