package com.loveyadav.traffic_pred.traffic_pred.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class RouteRequest {

    private Long userId;
    @NonNull
    private String source;
    @NonNull
    private String destination;
}
