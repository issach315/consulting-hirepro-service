package com.hirepro.users.service;

import com.hirepro.users.dto.AuthUserResponse;
import com.hirepro.users.dto.CreateAuthUserRequest;
import com.hirepro.users.dto.UpdateAuthUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthUserService {
    AuthUserResponse createUser(CreateAuthUserRequest request, String createdBy);
    AuthUserResponse updateUser(String userId, UpdateAuthUserRequest request, String updatedBy);
    void deleteUser(String userId, String deletedBy);
    AuthUserResponse getUserById(String userId);
    AuthUserResponse getUserByEmail(String email);
    Page<AuthUserResponse> getUsersByClientId(String clientId, Pageable pageable);
}