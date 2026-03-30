package com.loveyadav.traffic_pred.traffic_pred.controller;

import com.loveyadav.traffic_pred.traffic_pred.dto.ApiResponse;
import com.loveyadav.traffic_pred.traffic_pred.dto.RouteRequest;
import com.loveyadav.traffic_pred.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.traffic_pred.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {
    @Autowired
    private TrafficService trafficService;

    @Autowired
    private TrafficRepository trafficRepository;

    @PostMapping("/predict")
    public ApiResponse<Object> predict(@RequestBody RouteRequest req){
        Object result = trafficService.getTrafficPrediction(req);
        return new ApiResponse<>("Prediction Successful",result,true);
    }

    @GetMapping("/history")
    public List<TrafficRecord> getHistory(){
        return trafficRepository.findAll();
    }
}
