package com.jaffnabasket.backend.user.controller;

import com.jaffnabasket.backend.user.dto.AssignRoleRequest;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.entity.UserStatus;
import com.jaffnabasket.backend.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin - Users", description = "SUPER_ADMIN-only user management: search, role assignment, suspension")
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "List/search users",
            description = "Returns a paginated list of all users, newest first. Pass `query` to filter by a substring "
                    + "match on email or phone; leave it blank to list everyone. Uses simple page/size pagination "
                    + "(sort is fixed to newest-first) - not the cursor-based style used by the product catalog."
    )
    @GetMapping
    public Page<UserResponse> search(
            @Parameter(description = "Optional substring to match against email or phone. Omit to list all users.")
            @RequestParam(required = false) String query,
            @Parameter(description = "0-based page number, default 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, default 20") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return adminUserService.search(query, pageable);
    }

    @Operation(
            summary = "Assign a role to a user",
            description = "Grants the given role (e.g. CATALOG_MANAGER, CUSTOMER_SUPPORT, SUPER_ADMIN) to the specified "
                    + "user. No effect if they already have it. Note: the target user's existing access token won't "
                    + "reflect this until they call /auth/refresh or log in again, since roles are baked into the JWT "
                    + "at issue time."
    )
    @PostMapping("/{id}/roles")
    public UserResponse assignRole(@Parameter(description = "UUID of the user to grant the role to") @PathVariable UUID id,
                                    @Valid @RequestBody AssignRoleRequest request) {
        return adminUserService.assignRole(id, request.roleName());
    }

    @Operation(
            summary = "Revoke a role from a user",
            description = "Removes the given role from the specified user, if they currently have it."
    )
    @DeleteMapping("/{id}/roles/{roleName}")
    public UserResponse revokeRole(@Parameter(description = "UUID of the user to revoke the role from") @PathVariable UUID id,
                                    @Parameter(description = "Exact role name, e.g. CATALOG_MANAGER") @PathVariable String roleName) {
        return adminUserService.revokeRole(id, roleName);
    }

    @Operation(
            summary = "Suspend a user account",
            description = "Sets the user's status to SUSPENDED, which blocks future logins for that account. Existing "
                    + "access tokens they already hold keep working until they naturally expire."
    )
    @PostMapping("/{id}/suspend")
    public UserResponse suspend(@Parameter(description = "UUID of the user to suspend") @PathVariable UUID id) {
        return adminUserService.setStatus(id, UserStatus.SUSPENDED);
    }

    @Operation(
            summary = "Reactivate a suspended user",
            description = "Sets the user's status back to ACTIVE, restoring their ability to log in."
    )
    @PostMapping("/{id}/reactivate")
    public UserResponse reactivate(@Parameter(description = "UUID of the user to reactivate") @PathVariable UUID id) {
        return adminUserService.setStatus(id, UserStatus.ACTIVE);
    }
}
