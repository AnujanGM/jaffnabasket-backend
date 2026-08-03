package com.jaffnabasket.backend.user.service;

import com.jaffnabasket.backend.exception.BadRequestException;
import com.jaffnabasket.backend.exception.ConflictException;
import com.jaffnabasket.backend.exception.ResourceNotFoundException;
import com.jaffnabasket.backend.user.dto.RegisterRequest;
import com.jaffnabasket.backend.user.dto.UpdateProfileRequest;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.entity.*;
import com.jaffnabasket.backend.user.repository.ProfileRepository;
import com.jaffnabasket.backend.user.repository.RoleRepository;
import com.jaffnabasket.backend.user.repository.UserRepository;
import com.jaffnabasket.backend.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = blankToNull(request.email());
        String phone = blankToNull(request.phone());
        if (email == null && phone == null) {
            throw new BadRequestException("Either email or phone must be provided");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone already registered");
        }

        User user = User.builder()
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.PENDING_VERIFICATION)
                .build();
        user = userRepository.save(user);

        Profile profile = Profile.builder().user(user).build();
        profile = profileRepository.save(profile);

        Role customerRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Default role " + DEFAULT_ROLE + " has not been seeded"));
        userRoleRepository.save(UserRole.builder().user(user).role(customerRole).build());

        return UserMapper.toResponse(user, profile, List.of(DEFAULT_ROLE));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toFullResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = profileRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (request.firstName() != null) {
            profile.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            profile.setLastName(request.lastName());
        }
        if (request.preferredLanguage() != null) {
            profile.setPreferredLanguage(request.preferredLanguage());
        }
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
        }
        profileRepository.save(profile);

        return toFullResponse(user);
    }

    private UserResponse toFullResponse(User user) {
        Profile profile = profileRepository.findById(user.getId()).orElse(null);
        List<String> roles = userRoleRepository.findByUser_Id(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();
        return UserMapper.toResponse(user, profile, roles);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
