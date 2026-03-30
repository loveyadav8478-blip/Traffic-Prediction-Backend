package com.loveyadav.traffic_pred.traffic_pred.controller;

import com.loveyadav.traffic_pred.traffic_pred.dto.UserDto;
import com.loveyadav.traffic_pred.traffic_pred.entity.BlockListedTokens;
import com.loveyadav.traffic_pred.traffic_pred.entity.Role;
import com.loveyadav.traffic_pred.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.traffic_pred.repository.BlockListRepository;
import com.loveyadav.traffic_pred.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.traffic_pred.repository.UserRepository;
import com.loveyadav.traffic_pred.traffic_pred.service.RedisService;
import com.loveyadav.traffic_pred.traffic_pred.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RequestMapping("api/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TrafficRepository trafficRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BlockListRepository blackListRepository;
    @Autowired
    private RedisService redisService;

    @PostMapping("/register")
    public User register(@RequestBody UserDto userDto){
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDto.setRole(userDto.getRole());
        return userService.register(userDto);
    }

    @PostMapping("/login")
    public Map<String,String> login(@RequestBody User user){
        return userService.login(user.getEmail(), user.getPassword());
    }

    @GetMapping("/user-history/{userId}")
    public List<TrafficRecord> getUserHistory(@PathVariable Long userId){
        return trafficRepository.findByUserId(userId);
    }

    @DeleteMapping("/logout")
    public String logout(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            redisService.saveToken(token,15*60*1000);
//            BlockListedTokens bt = new BlockListedTokens();
//            bt.setToken(token);
//            blackListRepository.save(bt);
        }
        return "Logout Successful";
    }
}
