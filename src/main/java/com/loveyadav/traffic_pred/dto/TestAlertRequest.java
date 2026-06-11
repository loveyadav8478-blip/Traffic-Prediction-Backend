package com.loveyadav.traffic_pred.dto;

import com.loveyadav.traffic_pred.alert.AlertType;
import com.loveyadav.traffic_pred.alert.Severity;

import lombok.Data;

@Data
public class TestAlertRequest {
    private AlertType type;
    private Severity severity;
    private String route;
}