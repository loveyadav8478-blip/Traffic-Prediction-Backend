package com.loveyadav.traffic_pred.service;

import com.loveyadav.traffic_pred.dto.GoogleAuthRequest;
import com.loveyadav.traffic_pred.dto.UserDto;
import com.loveyadav.traffic_pred.entity.Role;
import com.loveyadav.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.filters.JwtFilter;
import com.loveyadav.traffic_pred.repository.UserRepository;
import com.loveyadav.traffic_pred.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public User register(UserDto dto){
        User user = new User();
        if(userRepo.findByEmail(dto.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists!!");
        }
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        System.out.println("Register raw: " + dto.getPassword());
        user.setName(dto.getName());
        user.setRole(Role.ROLE_USER);
        return userRepo.save(user);
    }

    public Map<String, String> login(String email, String password) {

        User user = userRepo.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User Not Found!!"));

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        log.info("LOGIN PASSWORD LENGTH: " + password.length());
        log.info("LOGIN PASSWORD: '" + password + "'");
        System.out.println("LOGIN PASSWORD: '" + password + "'");
        // CORRECT PASSWORD CHECK
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getRole() == null) {
            throw new RuntimeException("Role not assigned");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

//        redisTemplate.opsForValue().set("refresh:" + email, refreshToken, 7, TimeUnit.DAYS);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }


    public Map<String, Object> loginWithGoogle(GoogleAuthRequest req) {
        // 1. Find existing user by email OR create a new one
        Optional<User> existing = userRepo.findByEmail(req.getEmail());

        User user;
        if (existing.isPresent()) {
            user = existing.get();
            // Update Google fields if this is first Google login for existing email account
            if (user.getGoogleId() == null) {
                user.setGoogleId(req.getGoogleId());
                user.setPicture(req.getPicture());
                userRepo.save(user);
            }
        } else {
            // New user — create account, no password needed
            user = new User();
            user.setEmail(req.getEmail());
            user.setName(req.getName());
            user.setGoogleId(req.getGoogleId());
            user.setPicture(req.getPicture());
            user.setRole(Role.ROLE_USER);
            user.setPassword(null);   // Google users have no password
            userRepo.save(user);
        }

        // 2. Issue our standard JWT (same as email login)
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 3. Return same shape as email login: { token, user }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id",      user.getId());
        userMap.put("name",    user.getName());
        userMap.put("email",   user.getEmail());
        userMap.put("picture", user.getPicture());
        userMap.put("role",    user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user",  userMap);
        return response;
    }
}
