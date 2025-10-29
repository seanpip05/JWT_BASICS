package com.example.jwt_basics1.config;

import com.example.jwt_basics1.service.CustomUserDetailsService;
import com.example.jwt_basics1.service.TokenBlacklistService;
import com.example.jwt_basics1.service.CustomLogoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.jwt_basics1.service.TokenBlacklistService;
import com.example.jwt_basics1.service.CustomLogoutHandler;
import com.example.jwt_basics1.config.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity(debug = true) // enable debug mode to see the security filter chain in action
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    @Autowired @Lazy
    private TokenBlacklistService tokenBlacklistService;
    @Autowired @Lazy
    private CustomLogoutHandler customLogoutHandler;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService, tokenBlacklistService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // we don't need csrf protection in jwt
        http
                .csrf(AbstractHttpConfigurer::disable)
                // add cors corsConfigurer
                .cors(cors -> {
                    // register cors configuration source, React app is running on localhost:5173
                    cors.configurationSource(request -> {
                        var corsConfig = new CorsConfiguration();
                        corsConfig.setAllowedOrigins(List.of("http://localhost:5173")); // vite dev server
                        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        corsConfig.setAllowedHeaders(List.of("*"));
                        return corsConfig;
                    });
                })


                // The SessionCreationPolicy.STATELESS setting means that the application will not create or use HTTP sessions.
                // This is a common configuration in RESTful APIs, especially when using token-based authentication like JWT (JSON Web Token).
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // adding a custom JWT authentication filter
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // Configuring authorization for HTTP requests
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login/**").permitAll()
                        .requestMatchers("/api/refresh_token").permitAll()
                        .requestMatchers("/api/refresh_token/**").permitAll()
                        .requestMatchers("/api/protected-message-admin").hasAnyRole("ADMIN")
                        .requestMatchers("/api/protected-message").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }

}
