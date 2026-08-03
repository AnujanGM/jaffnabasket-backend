package com.jaffnabasket.backend.security.service;

import com.jaffnabasket.backend.security.CustomUserPrincipal;
import com.jaffnabasket.backend.security.JwtService;
import com.jaffnabasket.backend.security.dto.LoginRequest;
import com.jaffnabasket.backend.security.dto.LoginResponse;
import com.jaffnabasket.backend.security.repository.PasswordResetTokenRepository;
import com.jaffnabasket.backend.user.entity.Session;
import com.jaffnabasket.backend.user.entity.User;
import com.jaffnabasket.backend.user.repository.SessionRepository;
import com.jaffnabasket.backend.user.repository.UserRepository;
import com.jaffnabasket.backend.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SimpleRateLimiter rateLimiter;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString("test-secret-key-that-is-long-enough-1234567890".getBytes());
        JwtService jwtService = new JwtService(secret, 15, 7);
        authService = new AuthService(authenticationManager, userRepository, userRoleRepository,
                sessionRepository, passwordResetTokenRepository, passwordEncoder, jwtService, rateLimiter);
    }

    @Test
    void loginIssuesTokenPairAndPersistsSession() {
        UUID userId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, "test@example.com", "hashed", List.of("CUSTOMER"), true);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.getReferenceById(userId)).thenReturn(User.builder().id(userId).build());
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse response = authService.login(request, "rate-key", "test-device");

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(sessionRepository).save(any(Session.class));
        verify(rateLimiter).checkAllowed("rate-key");
        verify(rateLimiter).recordSuccess("rate-key");
    }

    @Test
    void loginRecordsFailureOnBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");

        assertThatThrownBy(() -> authService.login(request, "rate-key", "test-device"))
                .isInstanceOf(BadCredentialsException.class);

        verify(rateLimiter).recordFailure("rate-key");
    }
}
