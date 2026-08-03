package com.jaffnabasket.backend.user.controller;

import com.jaffnabasket.backend.user.dto.AssignRoleRequest;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.entity.UserStatus;
import com.jaffnabasket.backend.user.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Page<UserResponse> search(@RequestParam(required = false) String query,
                                      @PageableDefault(size = 20) Pageable pageable) {
        return adminUserService.search(query, pageable);
    }

    @PostMapping("/{id}/roles")
    public UserResponse assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {
        return adminUserService.assignRole(id, request.roleName());
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    public UserResponse revokeRole(@PathVariable UUID id, @PathVariable String roleName) {
        return adminUserService.revokeRole(id, roleName);
    }

    @PostMapping("/{id}/suspend")
    public UserResponse suspend(@PathVariable UUID id) {
        return adminUserService.setStatus(id, UserStatus.SUSPENDED);
    }

    @PostMapping("/{id}/reactivate")
    public UserResponse reactivate(@PathVariable UUID id) {
        return adminUserService.setStatus(id, UserStatus.ACTIVE);
    }
}
