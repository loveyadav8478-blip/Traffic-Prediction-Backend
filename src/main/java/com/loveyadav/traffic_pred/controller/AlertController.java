package com.loveyadav.traffic_pred.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loveyadav.traffic_pred.dto.AlertDTO;
import com.loveyadav.traffic_pred.dto.TestAlertRequest;
import com.loveyadav.traffic_pred.alert.AlertType;
import com.loveyadav.traffic_pred.alert.Severity;
import com.loveyadav.traffic_pred.service.AlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /** GET /api/alerts — recent 50 alerts */
    @GetMapping
    public ResponseEntity<List<AlertDTO>> getRecentAlerts() {
        return ResponseEntity.ok(alertService.getRecentAlerts());
    }

    /**
     * POST /api/alerts/test — generate a sample alert.
     * Restricted to ADMIN role so it cannot be abused in production.
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertDTO> generateTestAlert(@RequestBody(required = false)
                                                      TestAlertRequest req) {
        AlertType type     = req != null && req.getType()     != null ? req.getType()     : AlertType.SYSTEM;
        Severity  severity = req != null && req.getSeverity() != null ? req.getSeverity() : Severity.MEDIUM;
        String    route    = req != null && req.getRoute()    != null ? req.getRoute()    : "Route 5";

        AlertDTO dto = switch (type) {
            case ACCIDENT     -> alertService.sendAccidentAlert(route, "Test accident alert.");
            case CONGESTION   -> alertService.sendCongestionAlert(route, 35);
            case WEATHER      -> alertService.sendWeatherAlert(route, "Heavy Rain", 18);
            case ROAD_CLOSURE -> alertService.sendRoadClosureAlert(route, "Test closure");
            case EMERGENCY    -> alertService.sendEmergencyAlert(route, "Test emergency corridor.");
            case ECO_SYSTEM   -> alertService.sendEcoSystemAlert(42.5, 60.0);
            default           -> alertService.sendSystemAlert("🔧 Test Alert",
                    "This is a test system notification.", severity);
        };

        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/alerts/broadcast — admin manual broadcast.
     */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertDTO> manualBroadcast(@RequestBody AlertDTO payload) {
        AlertDTO dto = alertService.broadcastAlert(
                payload.getType(),
                payload.getTitle(),
                payload.getMessage(),
                payload.getSeverity(),
                payload.getRoute()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * STOMP @MessageMapping — clients can send a message to /app/ping
     * and receive a pong on /topic/pong (useful for connection health-checks).
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, String> ping() {
        return Map.of("status", "pong", "ts", java.time.Instant.now().toString());
    }

    @PostMapping("/api/alerts/test")
    public ResponseEntity<?> testAlert() {

        alertService.sendAccidentAlert(
                "NH-24",
                "Truck collision reported near Hapur"
        );

        return ResponseEntity.ok("Alert sent");
    }
}