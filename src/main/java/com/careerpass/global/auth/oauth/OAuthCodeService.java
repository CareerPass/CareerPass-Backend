package com.careerpass.global.auth.oauth;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuthCodeService {

    private static final Duration TTL = Duration.ofSeconds(60);

    private record Entry(String email, Instant expiresAt) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public String issue(String email) {
        String code = UUID.randomUUID().toString();
        store.put(code, new Entry(email, Instant.now().plus(TTL)));
        return code;
    }

    public Optional<String> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        Entry entry = store.remove(code);
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            return Optional.empty();
        }
        return Optional.of(entry.email());
    }
}
