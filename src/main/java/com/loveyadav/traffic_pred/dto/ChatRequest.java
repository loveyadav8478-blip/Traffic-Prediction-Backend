package com.loveyadav.traffic_pred.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ChatRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 2000, message = "Message too long")
    private String message;

    private String sessionId;

    @Size(max = 10, message = "Max 10 history messages")
    private List<ConversationMessage> conversationHistory;

    // ── Getters / Setters ────────────────────────────────
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public List<ConversationMessage> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(List<ConversationMessage> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }

    // ── Inner DTO ────────────────────────────────────────
    public static class ConversationMessage {
        private String role;    // "user" | "assistant"
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}