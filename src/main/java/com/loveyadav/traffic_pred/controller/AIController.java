package com.loveyadav.traffic_pred.controller;

import com.loveyadav.traffic_pred.dto.ChatRequest;
import com.loveyadav.traffic_pred.dto.ChatResponse;
import com.loveyadav.traffic_pred.dto.DailyReport;
import com.loveyadav.traffic_pred.exception.AIServiceException;
import com.loveyadav.traffic_pred.service.AIService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for all AI Assistant endpoints.
 * Base path: /api/ai
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000",
        "${routex.frontend.url:http://localhost:5173}"})
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * POST /api/ai/chat
     * Main chat endpoint. Accepts a user message + optional conversation history.
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatRequest request) {
        log.info("POST /api/ai/chat | session={} | msg={}", request.getSessionId(), request.getMessage());
        try {
            ChatResponse response = aiService.chat(request);
            return ResponseEntity.ok(response);
        } catch (AIServiceException e) {
            log.error("AI service error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", e.getMessage(), "code", "AI_SERVICE_ERROR"));
        } catch (Exception e) {
            log.error("Unexpected error in /chat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred.", "code", "INTERNAL_ERROR"));
        }
    }

    /**
     * POST /api/ai/report
     * Generates the Daily Traffic Intelligence Report.
     */
    @PostMapping("/report")
    public ResponseEntity<?> generateReport() {
        log.info("POST /api/ai/report");
        try {
            DailyReport report = aiService.generateDailyReport();
            return ResponseEntity.ok(report);
        } catch (AIServiceException e) {
            log.error("AI report generation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", e.getMessage(), "code", "REPORT_GENERATION_FAILED"));
        } catch (Exception e) {
            log.error("Unexpected error in /report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate report.", "code", "INTERNAL_ERROR"));
        }
    }

    /**
     * GET /api/ai/health
     * Quick health check for the AI service.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RouteX AI Assistant",
                "model", "gemini-1.5-pro"
        ));
    }
}