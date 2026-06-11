package com.loveyadav.traffic_pred.controller;

import com.loveyadav.traffic_pred.dto.ApiResponse;
import com.loveyadav.traffic_pred.dto.RouteRequest;
import com.loveyadav.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.repository.UserRepository;
import com.loveyadav.traffic_pred.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {
    @Autowired
    private TrafficService trafficService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrafficRepository trafficRepository;

    @PostMapping("/predict")
    public ApiResponse<Object> predict(@RequestBody RouteRequest req){
        Object result = trafficService.getTrafficPrediction(req);
        return new ApiResponse<>("Prediction Successful",result,true);
    }

    @GetMapping("/history")
    public List<TrafficRecord> getHistory(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return trafficRepository.findByUser(user);
    }

    @DeleteMapping("/history/{id}")
    public String deleteRecordById(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        trafficService.deleteByUserId(id, email);
        return "Record deleted Successfully";
    }
}
