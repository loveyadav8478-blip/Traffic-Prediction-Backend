package com.loveyadav.traffic_pred.dto;

import java.time.LocalDateTime;

import com.loveyadav.traffic_pred.alert.AlertType;
import com.loveyadav.traffic_pred.alert.Severity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {

    private String id;
    private AlertType type;
    private String title;
    private String message;
    private Severity severity;
    private LocalDateTime timestamp;
    private String route;

    /** Convenience factory from entity */
    public static AlertDTO from(com.loveyadav.traffic_pred.entity.Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .type(alert.getType())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .severity(alert.getSeverity())
                .timestamp(alert.getTimestamp())
                .route(alert.getRoute())
                .build();
    }
}