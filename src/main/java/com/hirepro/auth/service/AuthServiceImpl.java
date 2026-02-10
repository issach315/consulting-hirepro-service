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
import com.hirepro.users.dto.UserResponse;
import com.hirepro.users.entity.User;
import com.hirepro.users.enums.AccountStatus;
import com.hirepro.users.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
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
            User user = userRepository.findByUsernameAndNotDeleted(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check account status
            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new UnauthorizedException("Account is not active. Status: " + user.getAccountStatus());
            }

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

            // Save refresh token
            saveRefreshToken(user.getUserId(), refreshToken);

            return new AuthResponse(accessToken, refreshToken, jwtUtil.getAccessTokenExpiration() / 1000);

        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Check if phone number exists
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists");
        }

        // Create new user
        User user = new User();
        user.setUserId(UlidGenerator.generate());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setCreatedBy("SELF");

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token
        RefreshToken token = refreshTokenRepository.findValidToken(refreshToken, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        // Get user
        User user = userRepository.findByIdAndNotDeleted(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check account status
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), user.getRole());

        return new AuthResponse(newAccessToken, refreshToken, jwtUtil.getAccessTokenExpiration() / 1000);
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsernameAndNotDeleted(username)
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

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedBy(user.getCreatedBy());
        response.setUpdatedBy(user.getUpdatedBy());
        response.setBlockedBy(user.getBlockedBy());
        response.setAccountStatus(user.getAccountStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}