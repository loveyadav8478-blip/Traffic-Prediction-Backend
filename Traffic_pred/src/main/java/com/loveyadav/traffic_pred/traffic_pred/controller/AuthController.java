package com.loveyadav.traffic_pred.traffic_pred.controller;

import com.loveyadav.traffic_pred.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.traffic_pred.repository.UserRepository;
import com.loveyadav.traffic_pred.traffic_pred.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.*;

public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/refresh")
    public Map<String, String> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email);
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(),user.getRole().name());
        return Map.of("accessToken", newAccessToken);
    }
}
