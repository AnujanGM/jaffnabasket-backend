package com.jaffnabasket.backend.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class CustomUserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final List<String> roles;
    private final boolean enabled;

    public CustomUserPrincipal(UUID id, String username, String passwordHash, List<String> roles, boolean enabled) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
