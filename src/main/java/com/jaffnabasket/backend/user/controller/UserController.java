package com.jaffnabasket.backend.user.controller;

import com.jaffnabasket.backend.security.CustomUserPrincipal;
import com.jaffnabasket.backend.user.dto.RegisterRequest;
import com.jaffnabasket.backend.user.dto.UpdateProfileRequest;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "Account registration and self-service profile management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new account with either an email or a phone number (at least one required) plus a "
                    + "password. The account starts in PENDING_VERIFICATION status and is automatically given the "
                    + "CUSTOMER role. Does not log the user in - call POST /auth/login afterwards to get tokens."
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @Operation(
            summary = "Get my profile",
            description = "Returns the profile of the currently authenticated user, resolved from the access token. "
                    + "No id needed - it's always \"you\"."
    )
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return userService.getById(principal.getId());
    }

    @Operation(
            summary = "Update my profile",
            description = "Updates fields on the authenticated user's own profile (first/last name, preferred language, "
                    + "avatar URL). Only the fields you include in the request body are changed; omitted fields are left as-is."
    )
    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getId(), request);
    }

    @Operation(
            summary = "Get any user by ID (admin only)",
            description = "Returns the profile of any user, looked up by their UUID. Requires the SUPER_ADMIN role."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public UserResponse getById(@Parameter(description = "UUID of the user to look up") @PathVariable UUID id) {
        return userService.getById(id);
    }
}
