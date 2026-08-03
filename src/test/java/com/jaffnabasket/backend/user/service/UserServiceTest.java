package com.jaffnabasket.backend.user.service;

import com.jaffnabasket.backend.exception.BadRequestException;
import com.jaffnabasket.backend.exception.ConflictException;
import com.jaffnabasket.backend.user.dto.RegisterRequest;
import com.jaffnabasket.backend.user.dto.UserResponse;
import com.jaffnabasket.backend.user.entity.*;
import com.jaffnabasket.backend.user.repository.ProfileRepository;
import com.jaffnabasket.backend.user.repository.RoleRepository;
import com.jaffnabasket.backend.user.repository.UserRepository;
import com.jaffnabasket.backend.user.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registersUserWithDefaultCustomerRole() {
        RegisterRequest request = new RegisterRequest("test@example.com", null, "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        Role customerRole = Role.builder().id(UUID.randomUUID()).name("CUSTOMER").build();
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.register(request);

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.status()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(response.roles()).containsExactly("CUSTOMER");
        verify(userRepository).save(argThat(user -> user.getPasswordHash().equals("hashed-password")));
    }

    @Test
    void rejectsRegistrationWithoutEmailOrPhone() {
        RegisterRequest request = new RegisterRequest(null, null, "password123");

        assertThatThrownBy(() -> userService.register(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("dup@example.com", null, "password123");
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request)).isInstanceOf(ConflictException.class);
    }
}
