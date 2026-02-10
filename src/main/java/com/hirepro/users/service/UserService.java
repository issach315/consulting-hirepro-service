package com.hirepro.users.service;

import com.hirepro.users.dto.CreateUserRequest;
import com.hirepro.users.dto.UpdateUserRequest;
import com.hirepro.users.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(CreateUserRequest request, String createdBy);

    UserResponse updateUser(String userId, UpdateUserRequest request, String updatedBy);

    void deleteUser(String userId, String deletedBy);

    UserResponse getUserById(String userId);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserByUsername(String username);

    UserResponse updateSelf(String username, UpdateUserRequest request);

}