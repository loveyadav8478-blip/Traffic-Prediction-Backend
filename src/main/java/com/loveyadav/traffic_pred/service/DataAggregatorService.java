package com.loveyadav.traffic_pred.service;

import com.loveyadav.traffic_pred.service.FastApiAuthService;
import com.loveyadav.traffic_pred.dto.TrafficContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class DataAggregatorService {

    // ── Your existing FastAPI config ──────────────────────
    @Value("${fastapi.predict-url}")
    private String PREDICT_URL;

    // Fixed API key (same as TrafficService)
    private static final String FASTAPI_KEY =
            "aa5d066ad3983b27133902f4931636477d7df15ac15188a756e7b9afa46fd35e";

    // ── Weather config ────────────────────────────────────
    @Value("${routex.weather.api-url:https://api.open-meteo.com/v1/forecast}")
    private String weatherApiUrl;

    @Value("${routex.weather.lat:28.6139}")
    private double lat;

    @Value("${routex.weather.lon:77.2090}")
    private double lon;

    @Autowired private RestTemplate    restTemplate;
    @Autowired private FastApiAuthService fastApiAuthService;

    // ── Route pairs to predict — add/edit your real routes ──
    // Format: { source, destination, distanceKm }
    private static final Object[][] ROUTE_PAIRS = {
            { "Delhi",     "Agra",       200.0 },
            { "Delhi",     "Noida",       20.0 },
            { "Delhi",     "Gurgaon",     32.0 },
            { "Delhi",     "Ghaziabad",   28.0 },
            { "Noida",     "Gurgaon",     45.0 },
    };

    // ══════════════════════════════════════════════════════
    // Main entry — called by AIService
    // ══════════════════════════════════════════════════════
    public TrafficContext aggregateAll() {
        TrafficContext ctx = new TrafficContext();
        ctx.setDataTimestamp(Instant.now().toString());

        // 1. Real weather from Open-Meteo (free, no key)
        TrafficContext.WeatherData weather = fetchWeather();
        ctx.setWeather(weather);

        // 2. Real predictions from your FastAPI for each route pair
        List<TrafficContext.RouteStatus>     routes      = new ArrayList<>();
        List<TrafficContext.TrafficPrediction> predictions = new ArrayList<>();
        fetchAllRoutePredictions(weather, routes, predictions);

        ctx.setRoutes(routes);
        ctx.setPredictions(predictions);

        // 3. Ecosystem metrics derived from route data
        ctx.setEcosystem(deriveEcosystem(routes));

        // 4. Incidents — empty until you have an incident API
        ctx.setIncidents(List.of());

        return ctx;
    }

    // ══════════════════════════════════════════════════════
    // 1. Fetch real weather from Open-Meteo
    // ══════════════════════════════════════════════════════
    @SuppressWarnings({"unchecked","rawtypes"})
    private TrafficContext.WeatherData fetchWeather() {
        try {
            String url = String.format(
                    "%s?latitude=%.4f&longitude=%.4f" +
                            "&current=temperature_2m,relative_humidity_2m," +
                            "wind_speed_10m,weather_code,apparent_temperature" +
                            "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                            "&timezone=auto&forecast_days=1",
                    weatherApiUrl, lat, lon);

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null) return fallbackWeather("Data unavailable");

            Map current = (Map) response.get("current");
            if (current == null) return fallbackWeather("No current block");

            int    code      = ((Number) current.getOrDefault("weather_code", 0)).intValue();
            String condition = mapWmoCode(code);
            double temp      = ((Number) current.getOrDefault("temperature_2m", 30)).doubleValue();
            double feels     = ((Number) current.getOrDefault("apparent_temperature", 30)).doubleValue();
            double humidity  = ((Number) current.getOrDefault("relative_humidity_2m", 60)).doubleValue();
            double wind      = ((Number) current.getOrDefault("wind_speed_10m", 10)).doubleValue();

            TrafficContext.WeatherData w = new TrafficContext.WeatherData();
            w.setCondition(condition);
            w.setTemperatureCelsius(temp);
            w.setFeelsCelsius(feels);
            w.setHumidity(humidity);
            w.setWindSpeedKmh(wind);
            w.setHeavyRain(code >= 63 && code <= 67 || code >= 80 && code <= 82);
            w.setFog(code == 45 || code == 48);

            // Daily forecast
            Map daily = (Map) response.get("daily");
            if (daily != null) {
                List maxList = (List) daily.get("temperature_2m_max");
                List minList = (List) daily.get("temperature_2m_min");
                if (maxList != null && !maxList.isEmpty()) {
                    w.setForecast(String.format("High %.0f°C / Low %.0f°C — %s",
                            ((Number) maxList.get(0)).doubleValue(),
                            ((Number) minList.get(0)).doubleValue(),
                            condition));
                }
            }

            log.info("Weather fetched: {} {}°C", condition, temp);
            return w;

        } catch (Exception e) {
            log.warn("Weather fetch failed: {}", e.getMessage());
            return fallbackWeather(e.getMessage());
        }
    }

    private TrafficContext.WeatherData fallbackWeather(String reason) {
        TrafficContext.WeatherData w = new TrafficContext.WeatherData();
        w.setCondition("Clear"); w.setTemperatureCelsius(32);
        w.setHumidity(60);       w.setWindSpeedKmh(10);
        w.setForecast("Weather data temporarily unavailable: " + reason);
        return w;
    }

    // ══════════════════════════════════════════════════════
    // 2. Call your real FastAPI for every route pair
    // ══════════════════════════════════════════════════════
    @SuppressWarnings({"unchecked","rawtypes"})
    private void fetchAllRoutePredictions(
            TrafficContext.WeatherData weather,
            List<TrafficContext.RouteStatus> routes,
            List<TrafficContext.TrafficPrediction> predictions) {

        // Build shared time features once
        LocalDateTime now    = LocalDateTime.now();
        int hour             = now.getHour();
        int dow              = now.getDayOfWeek().getValue() - 1; // Mon=0
        int isWeekend        = dow >= 5 ? 1 : 0;
        int isPeak           = ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 20)) ? 1 : 0;
        String weatherStr    = mapConditionToFastApi(weather.getCondition());

        // Get JWT token once (same as TrafficService)
        String token;
        try { token = fastApiAuthService.getToken(); }
        catch (Exception e) {
            log.error("FastAPI auth failed: {}", e.getMessage());
            routes.add(unknownRoute("Auth failed — check FastApiAuthService"));
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", FASTAPI_KEY);
        headers.setBearerAuth(token);

        for (Object[] pair : ROUTE_PAIRS) {
            String source      = (String) pair[0];
            String destination = (String) pair[1];
            double distanceKm  = (Double)  pair[2];

            try {
                // Build request body — exactly like TrafficService does
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("source",       source);
                body.put("destination",  destination);
                body.put("hour",         hour);
                body.put("day_of_week",  dow);
                body.put("is_weekend",   isWeekend);
                body.put("is_peak_hour", isPeak);
                body.put("weather",      weatherStr);
                body.put("distance_km",  distanceKm);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        PREDICT_URL, entity, Map.class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.warn("FastAPI non-200 for {}->{}", source, destination);
                    routes.add(unknownRoute(source + " → " + destination));
                    continue;
                }

                Map<String, Object> responseMap = response.getBody();
                Map<String, Object> bestRoute   = (Map<String, Object>) responseMap.get("best_route");

                if (bestRoute == null) {
                    log.warn("best_route null for {}->{}", source, destination);
                    routes.add(unknownRoute(source + " → " + destination));
                    continue;
                }

                // ── Parse FastAPI response fields ─────────────────
                int    finalPrediction = ((Number) bestRoute.get("final_prediction")).intValue();
                double confidence      = ((Number) bestRoute.get("confidence")).doubleValue();
                double dist            = ((Number) bestRoute.get("distance_km")).doubleValue();
                double duration        = ((Number) bestRoute.get("duration_min")).doubleValue();
                String trafficLabel    = bestRoute.containsKey("traffic_label")
                        ? (String) bestRoute.get("traffic_label")
                        : labelFromInt(finalPrediction);

                // ── Map to RouteStatus ────────────────────────────
                TrafficContext.RouteStatus rs = new TrafficContext.RouteStatus();
                rs.setRouteName(source + " → " + destination);
                rs.setCongestionLevel(trafficLabel.toUpperCase()); // LOW/MEDIUM/HIGH
                rs.setAverageSpeedKmh(speedFromLabel(trafficLabel));
                rs.setDelayMinutes(delayFromLabel(trafficLabel));
                rs.setDistanceKm(dist);
                rs.setEstimatedDurationMin(duration);
                rs.setStatus("OPEN");
                rs.setPrimaryCause(causeFromLabel(trafficLabel, weather));
                rs.setConfidence(confidence);
                routes.add(rs);

                // ── Also add as prediction for next slot ──────────
                String nextSlot = nextHourSlot(hour);
                TrafficContext.TrafficPrediction pred = new TrafficContext.TrafficPrediction();
                pred.setRouteName(source + " → " + destination);
                pred.setTimeSlot(nextSlot);
                pred.setPredictedCongestion(trafficLabel.toUpperCase());
                pred.setConfidenceScore(confidence);
                predictions.add(pred);

                log.info("Route {}->{}: {} ({:.0f}% conf)",
                        source, destination, trafficLabel, confidence * 100);

            } catch (Exception e) {
                log.warn("FastAPI call failed for {}->{}: {}", source, destination, e.getMessage());
                routes.add(unknownRoute(source + " → " + destination));
            }
        }
    }

    // ══════════════════════════════════════════════════════
    // 3. Derive ecosystem metrics from route data
    //    (calculated — no separate API needed)
    // ══════════════════════════════════════════════════════
    private TrafficContext.EcosystemMetrics deriveEcosystem(
            List<TrafficContext.RouteStatus> routes) {

        TrafficContext.EcosystemMetrics m = new TrafficContext.EcosystemMetrics();

        if (routes.isEmpty() || routes.stream().allMatch(r -> "UNKNOWN".equals(r.getCongestionLevel()))) {
            m.setHealthScore(0);
            m.setTrend("UNKNOWN");
            m.setPrimaryConcern("No live route data available");
            return m;
        }

        long total  = routes.stream().filter(r -> !"UNKNOWN".equals(r.getCongestionLevel())).count();
        long low    = routes.stream().filter(r -> "LOW".equals(r.getCongestionLevel())).count();
        long medium = routes.stream().filter(r -> "MEDIUM".equals(r.getCongestionLevel())).count();
        long high   = routes.stream().filter(r -> "HIGH".equals(r.getCongestionLevel())).count();

        // Score: LOW=100pts, MEDIUM=55pts, HIGH=10pts → weighted average
        int score = total > 0
                ? (int) Math.round((low * 100.0 + medium * 55.0 + high * 10.0) / total)
                : 0;

        // Estimate CO2: HIGH congestion = ~2.5x more emissions than free flow
        double totalDistKm = routes.stream()
                .filter(r -> r.getDistanceKm() > 0)
                .mapToDouble(TrafficContext.RouteStatus::getDistanceKm)
                .sum();
        double co2Factor   = (high * 2.5 + medium * 1.5 + low * 1.0);
        double co2         = Math.round(totalDistKm * co2Factor * 0.21 * 10.0) / 10.0; // 0.21 kg CO2/km
        double fuelWasted  = Math.round(high * 4.2 * 10.0) / 10.0; // ~4.2L wasted per high route

        double flowEfficiency = total > 0 ? Math.round((low * 100.0 + medium * 60.0) / total) : 0;

        String trend    = high > total / 2 ? "DEGRADING" : low > total / 2 ? "IMPROVING" : "STABLE";
        String concern  = high > 0
                ? "High congestion on " + high + " route(s) increasing emissions"
                : medium > 0 ? "Moderate congestion detected" : "All routes flowing smoothly";

        m.setHealthScore(Math.min(score, 100));
        m.setCo2EmissionsToday(co2);
        m.setFuelWastedLiters(fuelWasted);
        m.setAvgFlowEfficiency(flowEfficiency);
        m.setTrend(trend);
        m.setPrimaryConcern(concern);

        log.info("Ecosystem: score={} trend={} high={} medium={} low={}", score, trend, high, medium, low);
        return m;
    }

    // ══════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════

    private String mapWmoCode(int code) {
        if (code == 0 || code == 1) return "Clear";
        if (code == 2)              return "Partly Cloudy";
        if (code == 3)              return "Overcast";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 55) return "Drizzle";
        if (code >= 61 && code <= 65) return "Rain";
        if (code >= 71 && code <= 75) return "Snow";
        if (code >= 80 && code <= 82) return "Heavy Rain";
        if (code >= 95)               return "Thunderstorm";
        return "Clear";
    }

    /** Map Open-Meteo condition → FastAPI weather string (same as TrafficService) */
    private String mapConditionToFastApi(String condition) {
        if (condition == null) return "Clear";
        return switch (condition.toLowerCase()) {
            case "rain", "heavy rain", "drizzle" -> "Rain";
            case "partly cloudy", "overcast"     -> "Clouds";
            case "fog"                           -> "Fog";
            case "snow", "heavy snow"            -> "Snow";
            case "thunderstorm"                  -> "Thunderstorm";
            default                              -> "Clear";
        };
    }

    private String labelFromInt(int val) {
        return switch (val) { case 0 -> "Low"; case 1 -> "Medium"; case 2 -> "High"; default -> "Low"; };
    }

    private double speedFromLabel(String label) {
        return switch (label.toLowerCase()) { case "high" -> 22.0; case "medium" -> 48.0; default -> 72.0; };
    }

    private int delayFromLabel(String label) {
        return switch (label.toLowerCase()) { case "high" -> 22; case "medium" -> 8; default -> 2; };
    }

    private String causeFromLabel(String label, TrafficContext.WeatherData w) {
        boolean badWeather = w.isHeavyRain() || w.isFog();
        return switch (label.toLowerCase()) {
            case "high"   -> badWeather ? "Heavy traffic + adverse weather" : "Peak hour congestion";
            case "medium" -> badWeather ? "Moderate traffic + weather impact" : "Moderate volume";
            default       -> badWeather ? "Clear flow (weather advisory)" : "Free flow";
        };
    }

    private String nextHourSlot(int hour) {
        int next = (hour + 1) % 24;
        return String.format("%02d:00–%02d:00", hour, next);
    }

    private TrafficContext.RouteStatus unknownRoute(String name) {
        TrafficContext.RouteStatus r = new TrafficContext.RouteStatus();
        r.setRouteName(name);
        r.setCongestionLevel("UNKNOWN");
        r.setStatus("UNKNOWN");
        r.setPrimaryCause("FastAPI call failed");
        return r;
    }
}