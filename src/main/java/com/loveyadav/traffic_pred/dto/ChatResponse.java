package com.loveyadav.traffic_pred.dto;

import java.time.Instant;
import java.util.List;

public class ChatResponse {

    private String answer;
    private String sessionId;
    private String timestamp;
    private Double confidence;
    private List<String> sources;
    private String model;

    private ChatResponse() {}

    public static Builder builder() { return new Builder(); }

    // ── Builder ──────────────────────────────────────────
    public static class Builder {
        private final ChatResponse r = new ChatResponse();

        public Builder answer(String answer)            { r.answer = answer; return this; }
        public Builder sessionId(String sessionId)      { r.sessionId = sessionId; return this; }
        public Builder confidence(Double confidence)    { r.confidence = confidence; return this; }
        public Builder sources(List<String> sources)    { r.sources = sources; return this; }
        public Builder model(String model)              { r.model = model; return this; }
        public ChatResponse build() {
            r.timestamp = Instant.now().toString();
            return r;
        }
    }

    // ── Getters ──────────────────────────────────────────
    public String getAnswer()      { return answer; }
    public String getSessionId()   { return sessionId; }
    public String getTimestamp()   { return timestamp; }
    public Double getConfidence()  { return confidence; }
    public List<String> getSources() { return sources; }
    public String getModel()       { return model; }
}