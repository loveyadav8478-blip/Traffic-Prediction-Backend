package com.loveyadav.traffic_pred.traffic_pred.dto;

import lombok.Data;
import lombok.Getter;

//@Data
@Getter
public class ApiResponse<T> {
    private String message;
    private T data;
    private boolean success;

    public ApiResponse(String message, T data, boolean success){
        this.data = data;
        this.success = success;
        this.message = message;
    }

}
