package com.jaffnabasket.backend.security.controller;

import com.jaffnabasket.backend.security.CustomUserPrincipal;
import com.jaffnabasket.backend.security.dto.*;
import com.jaffnabasket.backend.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Auth", description = "Login, token refresh, logout, and password reset")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Log in",
            description = "Validates an email/phone + password and, on success, issues a short-lived access token "
                    + "(15 min, used in the Authorization header) and a longer-lived refresh token (7 days, used only "
                    + "against /auth/refresh). Also records a new session row so the refresh token can be revoked later."
    )
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String rateLimitKey = "login:" + clientIp(httpRequest) + ":" + request.identifier();
        String deviceInfo = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        return authService.login(request, rateLimitKey, deviceInfo);
    }

    @Operation(
            summary = "Refresh the access token",
            description = "Exchanges a valid, non-revoked refresh token for a brand new access + refresh token pair, "
                    + "without needing the password again. The old refresh token is invalidated immediately (rotation) "
                    + "- always use the newest one returned. Also picks up any role changes made since the last login."
    )
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @Operation(
            summary = "Log out",
            description = "Revokes the session matching the given refresh token. Does not need an access token or "
                    + "Authorization header - just the refresh token in the body."
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Log out of all sessions",
            description = "Revokes every active session for the currently authenticated user (e.g. logging out all "
                    + "devices at once). Requires a valid access token in the Authorization header."
    )
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal CustomUserPrincipal principal) {
        authService.logoutAll(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Request a password reset",
            description = "Generates a short-lived reset token for the account matching the given email/phone, if one "
                    + "exists. Always responds 202 regardless of whether the identifier is registered, to avoid leaking "
                    + "which accounts exist. Email/SMS sending isn't wired up yet - the token is written to the server "
                    + "console log instead."
    )
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
        String rateLimitKey = "reset:" + clientIp(httpRequest) + ":" + request.identifier();
        authService.requestPasswordReset(request, rateLimitKey);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Confirm a password reset",
            description = "Sets a new password using a valid, unexpired, not-yet-used reset token from the request "
                    + "step, and revokes every existing session for that user as a security measure."
    )
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
