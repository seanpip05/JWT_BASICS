package com.example.jwt_basics1.service;

import com.example.jwt_basics1.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutSuccessHandler {
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        if (token != null) {
            Instant expiry = jwtUtil.extractExpiration(token).toInstant();
            tokenBlacklistService.blacklistToken(token, expiry);
        }
        // Optionally, handle refresh token blacklist if sent
        String refreshToken = request.getParameter("refreshToken");
        if (refreshToken != null) {
            tokenBlacklistService.blacklistRefreshToken(refreshToken);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Logout successful. Token blacklisted.");
    }
}

