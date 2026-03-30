package com.loveyadav.traffic_pred.traffic_pred.dto;

import lombok.Data;

import java.util.Map;

@Data
public class RouteResponse {

    private Map<String,Object> bestRoutes;
    private Object allRoutes;
}
