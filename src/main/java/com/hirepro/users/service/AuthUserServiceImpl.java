package com.hirepro.users.service;

import com.hirepro.common.exception.BadRequestException;
import com.hirepro.common.exception.ResourceNotFoundException;
import com.hirepro.common.util.UlidGenerator;
import com.hirepro.users.dto.AuthUserResponse;
import com.hirepro.users.dto.CreateAuthUserRequest;
import com.hirepro.users.dto.UpdateAuthUserRequest;
import com.hirepro.users.entity.AuthUser;
import com.hirepro.users.repository.AuthUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public AuthUserServiceImpl(AuthUserRepository authUserRepository,
                               PasswordEncoder passwordEncoder,
                               ModelMapper modelMapper) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public AuthUserResponse createUser(CreateAuthUserRequest request, String createdBy) {

        // Check if email already exists
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Validate employee type based on role
        validateEmployeeType(request.getRole(), request.getEmployeeType());

        AuthUser authUser = modelMapper.map(request, AuthUser.class);

        // Generate ULID for user ID
        authUser.setId(UlidGenerator.generate());
        authUser.setPassword(passwordEncoder.encode(request.getPassword()));
        authUser.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        authUser.setCreatedBy(createdBy);

        // SUPERADMIN must have null client_id
        if ("SUPERADMIN".equals(request.getRole())) {
            authUser.setClientId(null);
            authUser.setEmployeeType(null);
        }

        AuthUser savedUser = authUserRepository.save(authUser);
        return mapToResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthUserResponse updateUser(String userId, UpdateAuthUserRequest request, String updatedBy) {

        AuthUser authUser = authUserRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if email already exists (if being updated)
        if (request.getEmail() != null &&
                !request.getEmail().equals(authUser.getEmail()) &&
                authUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Validate employee type if role is being updated
        if (request.getRole() != null) {
            validateEmployeeType(request.getRole(),
                    request.getEmployeeType() != null ? request.getEmployeeType() : authUser.getEmployeeType());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            authUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        modelMapper.map(request, authUser);
        authUser.setUpdatedBy(updatedBy);

        AuthUser updatedUser = authUserRepository.save(authUser);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String userId, String deletedBy) {

        AuthUser authUser = authUserRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        authUser.setDeletedAt(LocalDateTime.now());
        authUser.setUpdatedBy(deletedBy);

        authUserRepository.save(authUser);
    }

    @Override
    public AuthUserResponse getUserById(String userId) {

        AuthUser authUser = authUserRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToResponse(authUser);
    }

    @Override
    public AuthUserResponse getUserByEmail(String email) {

        AuthUser authUser = authUserRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return mapToResponse(authUser);
    }

    @Override
    public Page<AuthUserResponse> getUsersByClientId(String clientId, Pageable pageable) {

        return authUserRepository.findByClientIdAndNotDeleted(clientId, pageable)
                .map(this::mapToResponse);
    }

    private void validateEmployeeType(String role, String employeeType) {
        // Roles that don't require employee type
        if ("SUPERADMIN".equals(role) || "CLIENT_ADMIN".equals(role)) {
            if (employeeType != null) {
                throw new BadRequestException(role + " should not have employee type");
            }
            return;
        }

        // Roles that require employee type
        if (employeeType == null) {
            throw new BadRequestException("Employee type is required for role: " + role);
        }

        if (!"DOMESTIC".equals(employeeType) && !"USIT".equals(employeeType)) {
            throw new BadRequestException("Employee type must be DOMESTIC or USIT");
        }
    }

    private AuthUserResponse mapToResponse(AuthUser authUser) {
        AuthUserResponse response = modelMapper.map(authUser, AuthUserResponse.class);
        // Don't return password
        return response;
    }
}