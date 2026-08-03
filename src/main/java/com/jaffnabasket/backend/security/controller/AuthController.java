package com.jaffnabasket.backend.security.controller;

import com.jaffnabasket.backend.security.CustomUserPrincipal;
import com.jaffnabasket.backend.security.dto.*;
import com.jaffnabasket.backend.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String rateLimitKey = "login:" + clientIp(httpRequest) + ":" + request.identifier();
        String deviceInfo = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        return authService.login(request, rateLimitKey, deviceInfo);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal CustomUserPrincipal principal) {
        authService.logoutAll(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
        String rateLimitKey = "reset:" + clientIp(httpRequest) + ":" + request.identifier();
        authService.requestPasswordReset(request, rateLimitKey);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
