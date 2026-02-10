package com.hirepro.users.controller;

import com.hirepro.common.response.ApiResponse;
import com.hirepro.users.dto.CreateUserRequest;
import com.hirepro.users.dto.UpdateUserRequest;
import com.hirepro.users.dto.UserResponse;
import com.hirepro.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------------------- ADMIN APIs --------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        UserResponse userResponse = userService.createUser(request, createdBy);
        return new ResponseEntity<>(
                ApiResponse.success("User created successfully", userResponse),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String updatedBy = authentication.getName();

        UserResponse userResponse = userService.updateUser(userId, request, updatedBy);
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", userResponse)
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String deletedBy = authentication.getName();

        userService.deleteUser(userId, deletedBy);
        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully")
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {

        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", userResponse)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction =
                sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    // -------------------- SELF APIs --------------------

    /**
     * Get current logged-in user details
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserResponse userResponse = userService.getUserByUsername(username);
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", userResponse)
        );
    }

    /**
     * Update current logged-in user's profile
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateUserRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserResponse userResponse = userService.updateSelf(username, request);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", userResponse)
        );
    }

}
