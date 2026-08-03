package com.jaffnabasket.backend.security.service;

import com.jaffnabasket.backend.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory fixed-window counter for login and password-reset attempts.
 * TODO: production-grade rate limiting needs a shared store (e.g. Redis) so
 * limits are enforced correctly across multiple app instances; this only
 * works for a single-instance deployment.
 */
@Component
public class SimpleRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Attempts> attemptsByKey = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Attempts attempts = attemptsByKey.get(key);
        if (attempts != null
                && Instant.now().isBefore(attempts.windowStart().plus(WINDOW))
                && attempts.count().get() >= MAX_ATTEMPTS) {
            throw new TooManyRequestsException("Too many attempts, please try again later");
        }
    }

    public void recordFailure(String key) {
        attemptsByKey.compute(key, (k, existing) -> {
            if (existing == null || Instant.now().isAfter(existing.windowStart().plus(WINDOW))) {
                return new Attempts(Instant.now(), new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });
    }

    public void recordSuccess(String key) {
        attemptsByKey.remove(key);
    }

    private record Attempts(Instant windowStart, AtomicInteger count) {
    }
}
