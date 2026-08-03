package com.jaffnabasket.backend.security.service;

import com.jaffnabasket.backend.exception.BadRequestException;
import com.jaffnabasket.backend.exception.UnauthorizedException;
import com.jaffnabasket.backend.security.CustomUserPrincipal;
import com.jaffnabasket.backend.security.JwtService;
import com.jaffnabasket.backend.security.TokenHasher;
import com.jaffnabasket.backend.security.dto.*;
import com.jaffnabasket.backend.security.entity.PasswordResetToken;
import com.jaffnabasket.backend.security.repository.PasswordResetTokenRepository;
import com.jaffnabasket.backend.user.entity.Session;
import com.jaffnabasket.backend.user.entity.User;
import com.jaffnabasket.backend.user.repository.SessionRepository;
import com.jaffnabasket.backend.user.repository.UserRepository;
import com.jaffnabasket.backend.user.repository.UserRoleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration PASSWORD_RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SimpleRateLimiter rateLimiter;

    @Transactional
    public LoginResponse login(LoginRequest request, String rateLimitKey, String deviceInfo) {
        rateLimiter.checkAllowed(rateLimitKey);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
        } catch (AuthenticationException ex) {
            rateLimiter.recordFailure(rateLimitKey);
            throw ex;
        }
        rateLimiter.recordSuccess(rateLimitKey);

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        return issueTokenPair(principal.getId(), principal.getRoles(), deviceInfo);
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        Claims claims = parseRefreshClaims(request.refreshToken());
        String hash = TokenHasher.sha256(request.refreshToken());
        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not recognized"));
        if (session.getRevokedAt() != null || session.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        UUID userId = jwtService.getUserId(claims);
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("User not found"));
        List<String> roles = userRoleRepository.findByUser_Id(userId).stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();

        String newAccessToken = jwtService.generateAccessToken(userId, roles);
        String newRefreshToken = jwtService.generateRefreshToken(userId, roles);
        session.setRefreshTokenHash(TokenHasher.sha256(newRefreshToken));
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(jwtService.getRefreshTokenTtl()));
        sessionRepository.save(session);

        return new LoginResponse(newAccessToken, newRefreshToken, "Bearer", jwtService.getAccessTokenTtl().toSeconds());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String hash = TokenHasher.sha256(request.refreshToken());
        sessionRepository.findByRefreshTokenHash(hash).ifPresent(session -> {
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        sessionRepository.findByUser_IdAndRevokedAtIsNull(userId)
                .forEach(session -> session.setRevokedAt(Instant.now()));
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request, String rateLimitKey) {
        rateLimiter.checkAllowed(rateLimitKey);
        userRepository.findByEmail(request.identifier())
                .or(() -> userRepository.findByPhone(request.identifier()))
                .ifPresentOrElse(
                        user -> {
                            rateLimiter.recordSuccess(rateLimitKey);
                            issuePasswordResetToken(user);
                        },
                        () -> rateLimiter.recordFailure(rateLimitKey));
        // Response is identical whether or not the identifier matched, to avoid user enumeration.
    }

    private void issuePasswordResetToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(TokenHasher.sha256(rawToken))
                .expiresAt(Instant.now().plus(PASSWORD_RESET_TOKEN_TTL))
                .build();
        passwordResetTokenRepository.save(token);
        // TODO: send rawToken via email/SMS in production. Stubbed to a log line for now.
        log.info("Password reset requested for user {}. Dev-only token: {}", user.getId(), rawToken);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String hash = TokenHasher.sha256(request.token());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        sessionRepository.findByUser_IdAndRevokedAtIsNull(user.getId())
                .forEach(session -> session.setRevokedAt(Instant.now()));
    }

    private LoginResponse issueTokenPair(UUID userId, List<String> roles, String deviceInfo) {
        String accessToken = jwtService.generateAccessToken(userId, roles);
        String refreshToken = jwtService.generateRefreshToken(userId, roles);

        Session session = Session.builder()
                .user(userRepository.getReferenceById(userId))
                .refreshTokenHash(TokenHasher.sha256(refreshToken))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtService.getRefreshTokenTtl()))
                .deviceInfo(deviceInfo)
                .build();
        sessionRepository.save(session);

        return new LoginResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTokenTtl().toSeconds());
    }

    private Claims parseRefreshClaims(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseClaims(refreshToken);
        } catch (JwtException ex) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        if (!jwtService.isRefreshToken(claims)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        return claims;
    }
}
