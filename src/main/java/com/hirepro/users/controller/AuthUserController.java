package com.hirepro.users.controller;

import com.hirepro.common.response.ApiResponse;
import com.hirepro.users.dto.AuthUserResponse;
import com.hirepro.users.dto.CreateAuthUserRequest;
import com.hirepro.users.dto.UpdateAuthUserRequest;
import com.hirepro.users.service.AuthUserService;
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
public class AuthUserController {

    private final AuthUserService authUserService;

    public AuthUserController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<ApiResponse<AuthUserResponse>> createUser(
            @Valid @RequestBody CreateAuthUserRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        AuthUserResponse userResponse = authUserService.createUser(request, createdBy);
        return new ResponseEntity<>(
                ApiResponse.success("User created successfully", userResponse),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<ApiResponse<AuthUserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateAuthUserRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String updatedBy = authentication.getName();

        AuthUserResponse userResponse = authUserService.updateUser(userId, request, updatedBy);
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", userResponse)
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String deletedBy = authentication.getName();

        authUserService.deleteUser(userId, deletedBy);
        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully")
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getUserById(@PathVariable String userId) {

        AuthUserResponse userResponse = authUserService.getUserById(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", userResponse)
        );
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getUserByEmail(@PathVariable String email) {

        AuthUserResponse userResponse = authUserService.getUserByEmail(email);
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", userResponse)
        );
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuthUserResponse>>> getUsersByClientId(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuthUserResponse> users = authUserService.getUsersByClientId(clientId, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }
}