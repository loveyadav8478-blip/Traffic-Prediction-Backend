package com.loveyadav.traffic_pred.dto;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String email;
    private String name;
    private String googleId;   // Google's "sub"
    private String picture;
}