package com.loveyadav.traffic_pred.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loveyadav.traffic_pred.dto.AlertDTO;
import com.loveyadav.traffic_pred.entity.Alert;
import com.loveyadav.traffic_pred.alert.AlertType;
import com.loveyadav.traffic_pred.alert.Severity;
import com.loveyadav.traffic_pred.repository.AlertRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private static final String ALERT_TOPIC = "/topic/alerts";

    private final SimpMessagingTemplate messagingTemplate;
    private final AlertRepository alertRepository;

    // ── Public broadcast helpers ──────────────────────────────────────────────

    public AlertDTO sendAccidentAlert(String routeName, String detail) {
        return broadcastAlert(
                AlertType.ACCIDENT,
                "🚨 Accident Detected",
                String.format("Major collision reported on %s. %s", routeName, detail),
                Severity.HIGH,
                routeName
        );
    }

    public AlertDTO sendCongestionAlert(String routeName, int increasePercent) {
        Severity severity = increasePercent >= 50 ? Severity.CRITICAL
                : increasePercent >= 30 ? Severity.HIGH
                : increasePercent >= 15 ? Severity.MEDIUM
                : Severity.LOW;
        return broadcastAlert(
                AlertType.CONGESTION,
                "⚠ Congestion Warning",
                String.format("Traffic on %s is predicted to increase by %d%% in the next 20 minutes.",
                        routeName, increasePercent),
                severity,
                routeName
        );
    }

    public AlertDTO sendWeatherAlert(String routeName, String condition, int trafficImpactPercent) {
        return broadcastAlert(
                AlertType.WEATHER,
                "🌧 Weather Impact Alert",
                String.format("%s expected on %s — traffic may increase by %d%%.",
                        condition, routeName, trafficImpactPercent),
                Severity.MEDIUM,
                routeName
        );
    }

    public AlertDTO sendRoadClosureAlert(String routeName, String reason) {
        return broadcastAlert(
                AlertType.ROAD_CLOSURE,
                "🚧 Road Closure",
                String.format("%s is closed: %s. Seek alternate routes.", routeName, reason),
                Severity.HIGH,
                routeName
        );
    }

    public AlertDTO sendEmergencyAlert(String routeName, String detail) {
        return broadcastAlert(
                AlertType.EMERGENCY,
                "🚑 Emergency Vehicle Priority",
                String.format("Emergency corridor activated on %s. %s", routeName, detail),
                Severity.CRITICAL,
                routeName
        );
    }

    public AlertDTO sendEcoSystemAlert(double score, double threshold) {
        return broadcastAlert(
                AlertType.ECO_SYSTEM,
                "📈 Ecosystem Score Alert",
                String.format("Ecosystem health score dropped to %.1f (threshold: %.1f). Review required.",
                        score, threshold),
                Severity.HIGH,
                null
        );
    }

    public AlertDTO sendSystemAlert(String title, String message, Severity severity) {
        return broadcastAlert(AlertType.SYSTEM, title, message, severity, null);
    }

    // ── Core broadcast ────────────────────────────────────────────────────────

    @Transactional
    public AlertDTO broadcastAlert(AlertType type, String title, String message,
                                   Severity severity, String route) {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .title(title)
                .message(message)
                .severity(severity)
                .route(route)
                .timestamp(LocalDateTime.now())
                .broadcasted(true)
                .build();

        alertRepository.save(alert);
        AlertDTO dto = AlertDTO.from(alert);

        log.info("Broadcasting alert [{}] {} → {}", severity, type, title);
        messagingTemplate.convertAndSend(ALERT_TOPIC, dto);

        return dto;
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AlertDTO> getRecentAlerts() {
        return alertRepository.findTop50ByOrderByTimestampDesc()
                .stream()
                .map(AlertDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertDTO> getAlertsSince(LocalDateTime since) {
        return alertRepository.findByTimestampAfterOrderByTimestampDesc(since)
                .stream()
                .map(AlertDTO::from)
                .collect(Collectors.toList());
    }

    // ── Integration hooks ─────────────────────────────────────────────────────

    /**
     * Called by TrafficPredictionService when a prediction exceeds the threshold.
     * @param routeName  the route being predicted
     * @param predictedIncrease  percentage increase in traffic
     * @param threshold  the configured alert threshold (e.g. 20 %)
     */
    public void checkAndAlertCongestion(String routeName, int predictedIncrease, int threshold) {
        if (predictedIncrease >= threshold) {
            log.info("Traffic prediction threshold exceeded for {} (+{}%). Sending congestion alert.",
                    routeName, predictedIncrease);
            sendCongestionAlert(routeName, predictedIncrease);
        }
    }

    /**
     * Called by WeatherService when a weather event is detected.
     */
    public void checkAndAlertWeather(String routeName, String condition, int impactPercent, int threshold) {
        if (impactPercent >= threshold) {
            sendWeatherAlert(routeName, condition, impactPercent);
        }
    }

    /**
     * Called by EcoSystemService when the score drops below threshold.
     */
    public void checkAndAlertEcoSystem(double score, double threshold) {
        if (score < threshold) {
            sendEcoSystemAlert(score, threshold);
        }
    }
}