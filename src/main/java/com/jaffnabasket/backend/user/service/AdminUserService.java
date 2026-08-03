package com.jaffnabasket.backend.user.service;

import com.jaffnabasket.backend.exception.ResourceNotFoundException;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.entity.Profile;
import com.jaffnabasket.backend.user.entity.Role;
import com.jaffnabasket.backend.user.entity.User;
import com.jaffnabasket.backend.user.entity.UserRole;
import com.jaffnabasket.backend.user.entity.UserStatus;
import com.jaffnabasket.backend.user.repository.ProfileRepository;
import com.jaffnabasket.backend.user.repository.RoleRepository;
import com.jaffnabasket.backend.user.repository.UserRepository;
import com.jaffnabasket.backend.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> search(String query, Pageable pageable) {
        return userRepository.search(query, pageable).map(this::toResponse);
    }

    @Transactional
    public UserResponse assignRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        if (userRoleRepository.findByUser_IdAndRole_Id(userId, role.getId()).isEmpty()) {
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse revokeRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        userRoleRepository.deleteByUser_IdAndRole_Id(userId, role.getId());
        return toResponse(user);
    }

    @Transactional
    public UserResponse setStatus(UUID userId, UserStatus status) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        Profile profile = profileRepository.findById(user.getId()).orElse(null);
        List<String> roles = userRoleRepository.findByUser_Id(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();
        return UserMapper.toResponse(user, profile, roles);
    }
}
