package com.loveyadav.traffic_pred.config;

import com.loveyadav.traffic_pred.filters.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.*;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(jwtFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ── BUG 1 FIXED ───────────────────────────────────────────────────────
        // Before: one giant string with commas inside List.of() — Spring treated
        // the entire string as ONE origin, so every real origin was rejected.
        // After: each origin is a separate string argument.
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://traffic-prediction-fastapi.onrender.com",
                "http://52.66.6.123"           // fixed: removed the duplicate "http://"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // ── BUG 2 FIXED ───────────────────────────────────────────────
                        // WebSocket handshake URLs were hitting .anyRequest().authenticated()
                        // and returning 403.  Must be permitAll() so the STOMP CONNECT
                        // frame (which carries the JWT) can actually arrive.
                        // WebSocketSecurityConfig then handles token validation inside
                        // the STOMP layer — not at the HTTP layer.
                        .requestMatchers("/ws/**", "/ws/info/**").permitAll()

                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public REST
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/api/user/register", "/api/user/login", "/api/user/google").permitAll()

                        // ── BUG 3 FIXED ───────────────────────────────────────────────
                        // GET /api/alerts is called on mount by AlertBell to load history.
                        // It was hitting .anyRequest().authenticated() which is fine IF
                        // the JWT is present — but adding an explicit matcher makes the
                        // intent clear and avoids ordering surprises.
                        .requestMatchers(HttpMethod.GET,  "/api/alerts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/alerts/test").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/alerts/broadcast").hasAuthority("ROLE_ADMIN")

                        // Admin only
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/analytics/**").hasAuthority("ROLE_ADMIN")

                        // Authenticated
                        .requestMatchers("/api/traffic/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}