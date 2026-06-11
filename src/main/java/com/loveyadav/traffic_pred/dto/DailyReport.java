package com.loveyadav.traffic_pred.dto;
import java.util.List;

public class DailyReport {

    private String executiveSummary;
    private List<CongestionHotspot> congestionHotspots;
    private String weatherImpact;
    private List<IncidentSummary> incidents;
    private int ecosystemScore;
    private String ecosystemSummary;
    private List<String> recommendedActions;
    private String aiNarrative;
    private String generatedAt;

    public static class CongestionHotspot {
        private String routeName;
        private String level;
        private String reason;

        public String getRouteName() { return routeName; }
        public void setRouteName(String n) { this.routeName = n; }
        public String getLevel() { return level; }
        public void setLevel(String l) { this.level = l; }
        public String getReason() { return reason; }
        public void setReason(String r) { this.reason = r; }
    }

    public static class IncidentSummary {
        private String title;
        private String location;
        private String time;

        public String getTitle() { return title; }
        public void setTitle(String t) { this.title = t; }
        public String getLocation() { return location; }
        public void setLocation(String l) { this.location = l; }
        public String getTime() { return time; }
        public void setTime(String t) { this.time = t; }
    }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String s) { this.executiveSummary = s; }
    public List<CongestionHotspot> getCongestionHotspots() { return congestionHotspots; }
    public void setCongestionHotspots(List<CongestionHotspot> h) { this.congestionHotspots = h; }
    public String getWeatherImpact() { return weatherImpact; }
    public void setWeatherImpact(String w) { this.weatherImpact = w; }
    public List<IncidentSummary> getIncidents() { return incidents; }
    public void setIncidents(List<IncidentSummary> i) { this.incidents = i; }
    public int getEcosystemScore() { return ecosystemScore; }
    public void setEcosystemScore(int s) { this.ecosystemScore = s; }
    public String getEcosystemSummary() { return ecosystemSummary; }
    public void setEcosystemSummary(String s) { this.ecosystemSummary = s; }
    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> a) { this.recommendedActions = a; }
    public String getAiNarrative() { return aiNarrative; }
    public void setAiNarrative(String n) { this.aiNarrative = n; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String t) { this.generatedAt = t; }
}