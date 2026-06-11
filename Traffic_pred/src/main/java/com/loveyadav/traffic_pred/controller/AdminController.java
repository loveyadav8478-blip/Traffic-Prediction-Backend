package com.loveyadav.traffic_pred.controller;

import com.loveyadav.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.repository.UserRepository;
import com.loveyadav.traffic_pred.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrafficRepository trafficRepository;
    @Autowired
    private TrafficService trafficService;
    
    @GetMapping("/get-all-users")
    public List<User> getAllUsers(){
        List<User> all = userRepository.findAll();
        return all;
    }

    @GetMapping("/get-all-traffic-record")
    public List<TrafficRecord> getAllTrafficRecord(){
        return trafficRepository.findAll();
    }

    @DeleteMapping("/history/all")
    public String deleteAllRecords(){
        trafficService.deleteAllRecords();
        return "All Records deleted Successfully";
    }
}
