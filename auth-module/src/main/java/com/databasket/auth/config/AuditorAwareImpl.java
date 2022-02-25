package com.databasket.auth.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContext.getCurrentUser() == null ? "Anonymous" : SecurityContext.getCurrentUser().getUsername());
    }
}