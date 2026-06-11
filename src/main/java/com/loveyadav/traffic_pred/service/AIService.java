package com.loveyadav.traffic_pred.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveyadav.traffic_pred.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Orchestrates the full AI pipeline:
 *   1. Aggregate live data
 *   2. Build prompt
 *   3. Call Gemini
 *   4. Parse and return response
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final MistralService geminiService;
    private final PromptBuilderService promptBuilder;
    private final DataAggregatorService dataAggregator;
    private final ObjectMapper          objectMapper;

    public AIService(MistralService geminiService,
                     PromptBuilderService promptBuilder,
                     DataAggregatorService dataAggregator,
                     ObjectMapper objectMapper) {
        this.geminiService   = geminiService;
        this.promptBuilder   = promptBuilder;
        this.dataAggregator  = dataAggregator;
        this.objectMapper    = objectMapper;
    }

    // ── Chat ──────────────────────────────────────────────

    public ChatResponse chat(ChatRequest request) {
        log.info("AI chat — session: {} | message: {}", request.getSessionId(), request.getMessage());

        // 1. Fetch all live data
        TrafficContext ctx = dataAggregator.aggregateAll();

        // 2. Build prompt
        String prompt = promptBuilder.buildChatPrompt(
                request.getMessage(), ctx, request.getConversationHistory());

        // 3. Call Gemini
        String rawAnswer = geminiService.generate(prompt);

        // 4. Determine data sources used
        List<String> sources = resolveSources(ctx);

        // 5. Estimate confidence based on data availability
        double confidence = estimateConfidence(ctx);

        return ChatResponse.builder()
                .answer(rawAnswer)
                .sessionId(request.getSessionId())
                .sources(sources)
                .confidence(confidence)
                .model("gemini-1.5-pro")
                .build();
    }

    // ── Daily Report ──────────────────────────────────────

    public DailyReport generateDailyReport() {
        log.info("Generating daily traffic intelligence report");

        // 1. Aggregate data
        TrafficContext ctx = dataAggregator.aggregateAll();

        // 2. Build report prompt
        String prompt = promptBuilder.buildReportPrompt(ctx);

        // 3. Call Gemini — expects JSON
        String rawJson = geminiService.generate(prompt);

        // 4. Parse JSON response into DailyReport
        DailyReport report = parseReportJson(rawJson, ctx);
        report.setGeneratedAt(Instant.now().toString());

        return report;
    }

    // ── Helpers ───────────────────────────────────────────

    private DailyReport parseReportJson(String rawJson, TrafficContext ctx) {
        // Strip any markdown fences Gemini might add
        String clean = rawJson
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        try {
            return objectMapper.readValue(clean, DailyReport.class);
        } catch (Exception e) {
            log.warn("Could not parse Gemini JSON response, building fallback report: {}", e.getMessage());
            return buildFallbackReport(rawJson, ctx);
        }
    }

    private DailyReport buildFallbackReport(String rawText, TrafficContext ctx) {
        DailyReport report = new DailyReport();
        report.setExecutiveSummary(rawText.length() > 300 ? rawText.substring(0, 300) + "…" : rawText);
        report.setCongestionHotspots(
                ctx.getRoutes() == null ? List.of() :
                        ctx.getRoutes().stream()
                                .filter(r -> "HIGH".equals(r.getCongestionLevel()))
                                .map(r -> {
                                    DailyReport.CongestionHotspot h = new DailyReport.CongestionHotspot();
                                    h.setRouteName(r.getRouteName());
                                    h.setLevel(r.getCongestionLevel());
                                    h.setReason(r.getPrimaryCause() != null ? r.getPrimaryCause() : "Heavy traffic volume");
                                    return h;
                                }).toList());
        report.setWeatherImpact("Weather data: " + (ctx.getWeather() != null ? ctx.getWeather().getCondition() : "N/A"));
        report.setIncidents(List.of());
        report.setEcosystemScore(ctx.getEcosystem() != null ? ctx.getEcosystem().getHealthScore() : 0);
        report.setEcosystemSummary("Ecosystem data loaded from live sensors.");
        report.setRecommendedActions(List.of(
                "Monitor high-congestion corridors closely.",
                "Consider signal timing adjustments on peak routes.",
                "Alert incident response teams."
        ));
        report.setAiNarrative(rawText.length() > 200 ? rawText.substring(0, 200) + "…" : rawText);
        return report;
    }

    private List<String> resolveSources(TrafficContext ctx) {
        var sources = new java.util.ArrayList<String>();
        if (ctx.getWeather() != null && !"Unknown".equals(ctx.getWeather().getCondition()))
            sources.add("Live Weather");
        if (ctx.getRoutes() != null && ctx.getRoutes().size() > 1)
            sources.add("Route Data");
        if (ctx.getIncidents() != null && !ctx.getIncidents().isEmpty())
            sources.add("Incidents");
        if (ctx.getEcosystem() != null && ctx.getEcosystem().getHealthScore() > 0)
            sources.add("Ecosystem");
        if (ctx.getPredictions() != null && !ctx.getPredictions().isEmpty())
            sources.add("ML Predictions");
        return sources;
    }

    private double estimateConfidence(TrafficContext ctx) {
        int score = 0, total = 5;
        if (ctx.getWeather() != null && !"Unknown".equals(ctx.getWeather().getCondition())) score++;
        if (ctx.getRoutes() != null && ctx.getRoutes().size() > 1) score++;
        if (ctx.getIncidents() != null) score++;
        if (ctx.getEcosystem() != null && ctx.getEcosystem().getHealthScore() > 0) score++;
        if (ctx.getPredictions() != null && !ctx.getPredictions().isEmpty()) score++;
        return Math.round((double) score / total * 100) / 100.0;
    }
}