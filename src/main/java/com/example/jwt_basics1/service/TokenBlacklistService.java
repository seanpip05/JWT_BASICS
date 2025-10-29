package com.example.jwt_basics1.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class TokenBlacklistService {
    // Stores token -> expiration time
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    // Optionally, store refresh tokens separately if needed
    private final Set<String> blacklistedRefreshTokens = new ConcurrentSkipListSet<>();

    // Add access token to blacklist
    public void blacklistToken(String token, Instant expiry) {
        cleanUp();
        blacklistedTokens.put(token, expiry);
    }

    // Add refresh token to blacklist
    public void blacklistRefreshToken(String token) {
        cleanUp();
        blacklistedRefreshTokens.add(token);
    }

    // Check if access token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        cleanUp();
        return blacklistedTokens.containsKey(token);
    }

    // Check if refresh token is blacklisted
    public boolean isRefreshTokenBlacklisted(String token) {
        cleanUp();
        return blacklistedRefreshTokens.contains(token);
    }

    // Remove expired tokens from blacklist
    private void cleanUp() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        // Optionally, implement refresh token expiry cleanup if you store expiry
    }
}

