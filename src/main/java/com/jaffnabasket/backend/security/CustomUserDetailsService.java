package com.jaffnabasket.backend.security;

import com.jaffnabasket.backend.user.entity.User;
import com.jaffnabasket.backend.user.entity.UserStatus;
import com.jaffnabasket.backend.user.repository.UserRepository;
import com.jaffnabasket.backend.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<String> roles = userRoleRepository.findByUser_Id(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();

        boolean enabled = user.getStatus() == UserStatus.ACTIVE || user.getStatus() == UserStatus.PENDING_VERIFICATION;
        String username = user.getEmail() != null ? user.getEmail() : user.getPhone();

        return new CustomUserPrincipal(user.getId(), username, user.getPasswordHash(), roles, enabled);
    }
}
