package com.loveyadav.traffic_pred.traffic_pred.service;

import com.loveyadav.traffic_pred.traffic_pred.dto.UserDto;
import com.loveyadav.traffic_pred.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.traffic_pred.filters.JwtFilter;
import com.loveyadav.traffic_pred.traffic_pred.repository.UserRepository;
import com.loveyadav.traffic_pred.traffic_pred.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
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
        if(userRepo.findByEmail(dto.getEmail())!=null){
            throw new RuntimeException("Email already exists!!");
        }
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setRole(dto.getRole());
        return userRepo.save(user);
    }

    public Map<String,String> login(String email, String password){
        User user = userRepo.findByEmail(email);
        if(user == null && !user.getEmail().equals(email)){
            throw new RuntimeException("Invalid Credential");
        }
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken, 7, TimeUnit.DAYS);
        return Map.of("accessToken",accessToken,"refreshToken",refreshToken);
    }
}
