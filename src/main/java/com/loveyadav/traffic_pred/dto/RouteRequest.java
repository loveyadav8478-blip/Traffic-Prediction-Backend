package com.loveyadav.traffic_pred.dto;

import lombok.Data;

@Data
public class RouteRequest {

    private Long userId;
    private String source;
    private String destination;
    private String time;             // "HH:mm"
    private String weatherCondition; // "Cloudy", "Rainy", etc.
    private String dayOfWeek;        // "Monday", "Saturday", etc.
    private Double distance;
}