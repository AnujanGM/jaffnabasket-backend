package com.jaffnabasket.backend.user.service;

import com.jaffnabasket.backend.user.dto.ProfileResponse;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.entity.Profile;
import com.jaffnabasket.backend.user.entity.User;

import java.util.List;

final class UserMapper {

    private UserMapper() {
    }

    static UserResponse toResponse(User user, Profile profile, List<String> roles) {
        ProfileResponse profileResponse = profile == null ? null : new ProfileResponse(
                profile.getFirstName(), profile.getLastName(), profile.getPreferredLanguage(), profile.getAvatarUrl());
        return new UserResponse(user.getId(), user.getEmail(), user.getPhone(), user.getStatus(),
                profileResponse, roles, user.getCreatedAt());
    }
}
