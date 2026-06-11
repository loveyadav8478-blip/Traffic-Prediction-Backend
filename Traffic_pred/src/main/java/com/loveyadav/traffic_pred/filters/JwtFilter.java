//package com.loveyadav.traffic_pred.filters;
//
//import com.loveyadav.traffic_pred.service.RedisService;
//import com.loveyadav.traffic_pred.utils.JwtUtil;
//import jakarta.servlet.*;
//import jakarta.servlet.http.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Collections;
//
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//    @Autowired
//    private RedisService redisService;
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        String header = request.getHeader("Authorization");
//
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//
//            if (jwtUtil.validateToken(token) && !redisService.isBlacklisted(token)) {
//                String email = jwtUtil.extractEmail(token);
//                String role  = jwtUtil.extractRole(token);
//                if (role == null || role.isEmpty()) role = "ROLE_USER";
//
//                UsernamePasswordAuthenticationToken auth =
//                        new UsernamePasswordAuthenticationToken(
//                                email, null,
//                                Collections.singleton(new SimpleGrantedAuthority(role))
//                        );
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            } else {
//                // Token present but invalid/blacklisted → reject clearly
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Invalid or expired token");
//                return; // ← stop filter chain
//            }
//        }
//        // No header at all → let Spring Security handle it (will 403 if route needs auth)
//        chain.doFilter(request, response);
//    }
//}


package com.loveyadav.traffic_pred.filters;

import com.loveyadav.traffic_pred.service.RedisService;
import com.loveyadav.traffic_pred.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            boolean valid = false;
            boolean blacklisted = false;

            try {
                valid = jwtUtil.validateToken(token);
            } catch (Exception e) {
                System.out.println("JWT VALIDATION ERROR: " + e.getMessage());
            }

            try {
                blacklisted = redisService.isBlacklisted(token);
            } catch (Exception e) {
                System.out.println("REDIS ERROR: " + e.getMessage());
            }

            System.out.println("=================================");
            System.out.println("TOKEN: " + token);
            System.out.println("VALID: " + valid);
            System.out.println("BLACKLISTED: " + blacklisted);
            System.out.println("=================================");

            if (valid && !blacklisted) {

                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                if (role == null || role.isBlank()) {
                    role = "ROLE_USER";
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singleton(
                                        new SimpleGrantedAuthority(role)
                                )
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");

                response.getWriter().write(
                        "{\"error\":\"Invalid or expired token\",\"valid\":"
                                + valid +
                                ",\"blacklisted\":"
                                + blacklisted +
                                "}"
                );

                return;
            }
        }

        chain.doFilter(request, response);
    }
}