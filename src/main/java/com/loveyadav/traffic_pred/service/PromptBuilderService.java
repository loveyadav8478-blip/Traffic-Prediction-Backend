package com.loveyadav.traffic_pred.service;

import com.loveyadav.traffic_pred.dto.ChatRequest;
import com.loveyadav.traffic_pred.dto.TrafficContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds structured, context-rich prompts for Gemini.
 * Separates prompt engineering from business logic.
 */
@Service
public class PromptBuilderService {

    private static final String SYSTEM_PERSONA = """
        You are RouteX AI Assistant — an expert urban traffic intelligence analyst embedded in \
        the RouteX Smart Traffic Management Platform.
        
        YOUR CAPABILITIES:
        - Real-time traffic analysis and congestion diagnosis
        - Route comparison and optimised recommendations
        - Weather-traffic correlation analysis
        - Incident impact assessment
        - Ecosystem health and emissions analysis
        - Predictive traffic modelling explanation
        - City-level traffic intelligence for administrators
        
        YOUR RULES:
        - Always base answers on the LIVE DATA provided in the context below
        - Never fabricate route names, speeds, or incident data
        - If data is unavailable for a specific query, clearly say so
        - Format responses with clear structure using bullet points or numbered lists where helpful
        - Keep answers concise but complete — aim for 150-300 words unless a detailed analysis is requested
        - Always suggest actionable alternatives when congestion is identified
        - Consider weather impact proactively in every route recommendation
        - Use metric units (km, km/h, °C)
        """;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy HH:mm");

    // ── Chat Prompt ───────────────────────────────────────

    public String buildChatPrompt(
            String userMessage,
            TrafficContext ctx,
            List<ChatRequest.ConversationMessage> history) {

        StringBuilder sb = new StringBuilder();

        // Short system prompt — not the full paragraph
        sb.append("You are RouteX AI, a traffic assistant. ")
                .append("Answer using only the data below. Be concise (max 150 words). ")
                .append("Suggest alternate routes when congestion is high.\n\n");

        // Minimal weather
        if (ctx.getWeather() != null) {
            var w = ctx.getWeather();
            sb.append("Weather: ").append(w.getCondition())
                    .append(", ").append(w.getTemperatureCelsius()).append("°C")
                    .append(w.isHeavyRain() ? ", heavy rain" : "")
                    .append(w.isFog()       ? ", fog"        : "")
                    .append("\n");
        }

        // Only HIGH/MEDIUM routes — skip LOW
        if (ctx.getRoutes() != null) {
            ctx.getRoutes().stream()
                    .filter(r -> !"LOW".equals(r.getCongestionLevel()))
                    .limit(5)   // max 5 routes
                    .forEach(r -> sb.append("Route: ").append(r.getRouteName())
                            .append(" | ").append(r.getCongestionLevel())
                            .append(" | ").append(r.getAverageSpeedKmh()).append("km/h")
                            .append(" | +").append(r.getDelayMinutes()).append("min\n"));
        }

        // Only active incidents, max 3
        if (ctx.getIncidents() != null) {
            ctx.getIncidents().stream()
                    .filter(TrafficContext.Incident::isActive)
                    .limit(3)
                    .forEach(i -> sb.append("Incident: ").append(i.getTitle())
                            .append(" @ ").append(i.getLocation()).append("\n"));
        }

        // Ecosystem score only
        if (ctx.getEcosystem() != null) {
            sb.append("Ecosystem score: ").append(ctx.getEcosystem().getHealthScore()).append("/100\n");
        }

        // Last 3 messages only (not 10)
        if (history != null && !history.isEmpty()) {
            history.stream().skip(Math.max(0, history.size() - 3)).forEach(m ->
                    sb.append(m.getRole().equals("user") ? "User: " : "AI: ")
                            .append(m.getContent(), 0, Math.min(m.getContent().length(), 100))
                            .append("\n"));
        }

        sb.append("\nUser: ").append(userMessage).append("\nAI:");
        return sb.toString();
    }

    // ── Daily Report Prompt ───────────────────────────────

    public String buildReportPrompt(TrafficContext ctx) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== SYSTEM PERSONA ===\n").append(SYSTEM_PERSONA).append("\n\n");
        sb.append("=== REPORT GENERATION TASK ===\n");
        sb.append("Generate a comprehensive Daily Traffic Intelligence Report for city administrators.\n");
        sb.append("Date: ").append(LocalDateTime.now().format(DT_FMT)).append("\n\n");

        sb.append("=== LIVE ROUTEX DATA ===\n");
        appendTrafficContext(sb, ctx);

        sb.append("\n=== REPORT FORMAT ===\n");
        sb.append("""
            Generate the report as a valid JSON object with exactly these fields:
            {
              "executiveSummary": "2-3 sentence high-level summary of today's traffic situation",
              "congestionHotspots": [
                { "routeName": "Route X", "level": "HIGH|MEDIUM|LOW", "reason": "explanation" }
              ],
              "weatherImpact": "Detailed paragraph about how weather is affecting traffic today",
              "incidents": [
                { "title": "Incident title", "location": "Location", "time": "HH:MM" }
              ],
              "ecosystemScore": 75,
              "ecosystemSummary": "1-2 sentences about ecosystem health",
              "recommendedActions": [
                "Action 1 for administrators",
                "Action 2",
                "Action 3",
                "Action 4",
                "Action 5"
              ],
              "aiNarrative": "3-4 sentence AI analysis paragraph with key insights and predictions for the rest of the day"
            }
            
            Return ONLY the JSON object. No markdown, no code blocks, no extra text.
            """);

        return sb.toString();
    }

    // ── Context Serialiser ────────────────────────────────

    private void appendTrafficContext(StringBuilder sb, TrafficContext ctx) {

        // Weather
        if (ctx.getWeather() != null) {
            var w = ctx.getWeather();
            sb.append("WEATHER CONDITIONS:\n");
            sb.append("  Condition: ").append(w.getCondition()).append("\n");
            sb.append("  Temperature: ").append(w.getTemperatureCelsius()).append("°C\n");
            sb.append("  Humidity: ").append(w.getHumidity()).append("%\n");
            sb.append("  Wind: ").append(w.getWindSpeedKmh()).append(" km/h\n");
//            sb.append("  Visibility: ").append(w.getVisibility()).append("\n");
            sb.append("  Heavy Rain: ").append(w.isHeavyRain()).append("\n");
            sb.append("  Fog: ").append(w.isFog()).append("\n");
            if (w.getForecast() != null)
                sb.append("  Forecast: ").append(w.getForecast()).append("\n");
            sb.append("\n");
        }

        // Routes
        if (ctx.getRoutes() != null && !ctx.getRoutes().isEmpty()) {
            sb.append("ROUTE STATUS (").append(ctx.getRoutes().size()).append(" routes monitored):\n");
            for (var r : ctx.getRoutes()) {
                sb.append("  [").append(r.getRouteName()).append("]")
                        .append(" | Congestion: ").append(r.getCongestionLevel())
                        .append(" | Speed: ").append(r.getAverageSpeedKmh()).append(" km/h")
                        .append(" | Delay: ").append(r.getDelayMinutes()).append(" min")
                        .append(" | Distance: ").append(r.getDistanceKm()).append(" km")
                        .append(" | Status: ").append(r.getStatus());
                if (r.getPrimaryCause() != null && !r.getPrimaryCause().isBlank())
                    sb.append(" | Cause: ").append(r.getPrimaryCause());
                sb.append("\n");
            }
            sb.append("\n");
        }

        // Active incidents
        if (ctx.getIncidents() != null && !ctx.getIncidents().isEmpty()) {
            long active = ctx.getIncidents().stream().filter(TrafficContext.Incident::isActive).count();
            sb.append("ACTIVE INCIDENTS (").append(active).append(" active):\n");
            for (var i : ctx.getIncidents()) {
                if (!i.isActive()) continue;
                sb.append("  [").append(i.getSeverity()).append("] ")
                        .append(i.getTitle())
                        .append(" @ ").append(i.getLocation())
                        .append(" | Type: ").append(i.getType())
                        .append(" | Time: ").append(i.getTime());
                if (i.getAffectedRoutes() != null)
                    sb.append(" | Affects: ").append(i.getAffectedRoutes());
                sb.append("\n");
            }
            sb.append("\n");
        } else {
            sb.append("ACTIVE INCIDENTS: None reported\n\n");
        }

        // Ecosystem
        if (ctx.getEcosystem() != null) {
            var e = ctx.getEcosystem();
            sb.append("ECOSYSTEM HEALTH METRICS:\n");
            sb.append("  Health Score: ").append(e.getHealthScore()).append("/100\n");
            sb.append("  CO2 Emissions Today: ").append(e.getCo2EmissionsToday()).append(" kg\n");
            sb.append("  Fuel Wasted: ").append(e.getFuelWastedLiters()).append(" litres\n");
            sb.append("  Flow Efficiency: ").append(e.getAvgFlowEfficiency()).append("%\n");
            sb.append("  Trend: ").append(e.getTrend()).append("\n");
            if (e.getPrimaryConcern() != null)
                sb.append("  Primary Concern: ").append(e.getPrimaryConcern()).append("\n");
            sb.append("\n");
        }

        // Predictions
        if (ctx.getPredictions() != null && !ctx.getPredictions().isEmpty()) {
            sb.append("TRAFFIC PREDICTIONS (next slots):\n");
            for (var p : ctx.getPredictions()) {
                sb.append("  ").append(p.getRouteName())
                        .append(" @ ").append(p.getTimeSlot())
                        .append(": ").append(p.getPredictedCongestion())
                        .append(" (confidence: ").append(Math.round(p.getConfidenceScore() * 100)).append("%)\n");
            }
            sb.append("\n");
        }

        sb.append("Data timestamp: ").append(ctx.getDataTimestamp()).append("\n");
    }
}
