package com.hirepro.users.service;

import com.hirepro.common.exception.BadRequestException;
import com.hirepro.common.exception.ResourceNotFoundException;
import com.hirepro.common.util.UlidGenerator;
import com.hirepro.users.dto.CreateUserRequest;
import com.hirepro.users.dto.UpdateUserRequest;
import com.hirepro.users.dto.UserResponse;
import com.hirepro.users.entity.User;
import com.hirepro.users.enums.AccountStatus;
import com.hirepro.users.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    // ================= ADMIN APIs =================

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request, String createdBy) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (request.getPhoneNumber() != null
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists");
        }

        // Map DTO → Entity
        User user = modelMapper.map(request, User.class);

        // Set system-managed fields
        user.setUserId(UlidGenerator.generate());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(
                request.getAccountStatus() != null
                        ? request.getAccountStatus()
                        : AccountStatus.ACTIVE
        );
        user.setCreatedBy(createdBy);

        User savedUser = userRepository.save(user);

        // Map Entity → DTO
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String userId,
                                   UpdateUserRequest request,
                                   String updatedBy) {

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Handle uniqueness checks manually
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists");
        }

        // Map non-null fields from DTO → Entity
        modelMapper.map(request, user);

        if (request.getAccountStatus() == AccountStatus.BLOCKED) {
            user.setBlockedBy(updatedBy);
        }

        user.setUpdatedBy(updatedBy);

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserResponse.class);
    }

    @Override
    @Transactional
    public void deleteUser(String userId, String deletedBy) {

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedBy(deletedBy);

        userRepository.save(user);
    }

    @Override
    public UserResponse getUserById(String userId) {

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        return userRepository.findAllNotDeleted(pageable)
                .map(user -> modelMapper.map(user, UserResponse.class));
    }

    // ================= SELF APIs =================

    @Override
    public UserResponse getUserByUsername(String username) {

        User user = userRepository.findByUsernameAndNotDeleted(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional
    public UserResponse updateSelf(String username, UpdateUserRequest request) {

        User user = userRepository.findByUsernameAndNotDeleted(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Unique checks
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists");
        }

        // Update only allowed fields
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        user.setUpdatedBy(username);

        User updatedUser = userRepository.save(user);

        return modelMapper.map(updatedUser, UserResponse.class);
    }


}
