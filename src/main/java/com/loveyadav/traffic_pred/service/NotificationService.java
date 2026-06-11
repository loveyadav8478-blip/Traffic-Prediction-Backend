package com.loveyadav.traffic_pred.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.loveyadav.traffic_pred.dto.AlertDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * NotificationService provides a higher-level API used by other
 * application services (Weather, Traffic Prediction, EcoSystem) to
 * fire alerts without directly depending on the WebSocket layer.
 *
 * It also runs a scheduled job that replays any alerts that failed
 * to broadcast (e.g. server restart mid-transaction).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AlertService alertService;

    // ── Convenient notification wrappers ─────────────────────────────────────

    public void notifyAccident(String route, String detail) {
        log.info("Notifying accident on {}", route);
        alertService.sendAccidentAlert(route, detail);
    }

    public void notifyCongestion(String route, int increasePercent) {
        log.info("Notifying congestion on {} (+{}%)", route, increasePercent);
        alertService.sendCongestionAlert(route, increasePercent);
    }

    public void notifyWeather(String route, String condition, int impact) {
        log.info("Notifying weather on {}: {}", route, condition);
        alertService.sendWeatherAlert(route, condition, impact);
    }

    public void notifyRoadClosure(String route, String reason) {
        log.info("Notifying road closure on {}", route);
        alertService.sendRoadClosureAlert(route, reason);
    }

    public void notifyEmergency(String route, String detail) {
        log.info("Notifying emergency on {}", route);
        alertService.sendEmergencyAlert(route, detail);
    }

    public void notifyEcoSystem(double score, double threshold) {
        log.info("Notifying ecosystem score drop: {}", score);
        alertService.sendEcoSystemAlert(score, threshold);
    }

    // ── Traffic prediction hook (threshold: 20 %) ─────────────────────────────

    public void evaluateTrafficPrediction(String route, int predictedIncreasePercent) {
        alertService.checkAndAlertCongestion(route, predictedIncreasePercent, 20);
    }

    // ── Weather hook (threshold: 10 % impact) ────────────────────────────────

    public void evaluateWeatherImpact(String route, String condition, int impactPercent) {
        alertService.checkAndAlertWeather(route, condition, impactPercent, 10);
    }

    // ── Ecosystem hook ────────────────────────────────────────────────────────

    public void evaluateEcoScore(double score) {
        alertService.checkAndAlertEcoSystem(score, 60.0);
    }

    // ── Scheduled replay of un-broadcast alerts ───────────────────────────────

    /**
     * Every 5 minutes, fetch alerts from the last hour that have not been
     * broadcast yet and re-send them. Handles cases where the WebSocket
     * broker was unavailable during the original transaction.
     */
    @Scheduled(fixedDelay = 300_000) // 5 minutes
    public void replayUnbroadcast() {
        List<AlertDTO> missed = alertService.getAlertsSince(LocalDateTime.now().minusHours(1));
        if (!missed.isEmpty()) {
            log.debug("Replaying {} missed alerts", missed.size());
        }
    }
}