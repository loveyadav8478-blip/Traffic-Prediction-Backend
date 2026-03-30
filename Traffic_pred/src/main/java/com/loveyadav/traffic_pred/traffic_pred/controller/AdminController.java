package com.loveyadav.traffic_pred.traffic_pred.controller;

import com.loveyadav.traffic_pred.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.traffic_pred.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrafficRepository trafficRepository;
    
    @GetMapping("/get-all-users")
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @GetMapping("/get-all-traffic-record")
    public List<TrafficRecord> getAllTrafficRecord(){
        return trafficRepository.findAll();
    }
}
