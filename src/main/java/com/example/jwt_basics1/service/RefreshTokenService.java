package com.example.jwt_basics1.service;

import com.example.jwt_basics1.config.JwtUtil;
import com.example.jwt_basics1.dto.AuthenticationResponse;
import com.example.jwt_basics1.dto.RefreshTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    // For IP management
    private final Map<String, String> refreshTokenIpMap = new ConcurrentHashMap<>();

    public AuthenticationResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshToken = request.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        String clientIp = httpRequest.getRemoteAddr();

        // Blacklist check
        if (tokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token is blacklisted");
        }
        // IP check
        String storedIp = refreshTokenIpMap.get(refreshToken);
        if (storedIp == null || !storedIp.equals(clientIp)) {
            throw new RuntimeException("IP address mismatch for refresh token");
        }
        if (!jwtUtil.validateRefreshToken(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        // Blacklist old refresh token
        tokenBlacklistService.blacklistRefreshToken(refreshToken);
        // Generate new tokens
        String newAccessToken = jwtUtil.generateToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
        // Store new refresh token IP
        refreshTokenIpMap.put(newRefreshToken, clientIp);
        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }

    public void storeRefreshTokenIp(String refreshToken, String ip) {
        refreshTokenIpMap.put(refreshToken, ip);
    }
}
