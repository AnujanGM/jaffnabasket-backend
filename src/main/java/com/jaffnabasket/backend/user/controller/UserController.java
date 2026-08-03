package com.jaffnabasket.backend.user.controller;

import com.jaffnabasket.backend.security.CustomUserPrincipal;
import com.jaffnabasket.backend.user.dto.RegisterRequest;
import com.jaffnabasket.backend.user.dto.UpdateProfileRequest;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return userService.getById(principal.getId());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getId(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }
}
