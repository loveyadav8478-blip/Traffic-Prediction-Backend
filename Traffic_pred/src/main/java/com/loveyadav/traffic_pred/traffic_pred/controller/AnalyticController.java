package com.loveyadav.traffic_pred.traffic_pred.controller;

import java.util.*;
import com.loveyadav.traffic_pred.traffic_pred.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/analytics")
public class AnalyticController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/popular-routes")
    public List<Map<String,Object>> getPopularRoutes(){
        return analyticsService.getPopularRoutes();
    }

    @GetMapping("/traffic-stats")
    public List<Map<String,Object>> getTrafficStats(){
        return analyticsService.getTrafficStats();
    }

    @GetMapping("/peak-hours")
    public List<Map<String,Object>> getPeakHours(){
        return analyticsService.getPeakHours();
    }
}
