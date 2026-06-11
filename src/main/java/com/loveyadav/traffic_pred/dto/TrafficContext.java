package com.loveyadav.traffic_pred.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class TrafficContext {

    // ── Root getters/setters ──────────────────────────────
    private WeatherData             weather;
    private List<RouteStatus>       routes;
    private List<Incident>          incidents;
    private EcosystemMetrics        ecosystem;
    private List<TrafficPrediction> predictions;
    private String                  dataTimestamp;

    // ── WeatherData ───────────────────────────────────────
    public static class WeatherData {
        private String  condition;
        private double  temperatureCelsius;
        private double  feelsCelsius;       // NEW
        private double  humidity;
        private double  windSpeedKmh;
        private boolean heavyRain;
        private boolean fog;
        private String  forecast;

        public String  getCondition()           { return condition; }
        public void    setCondition(String c)   { this.condition = c; }
        public double  getTemperatureCelsius()  { return temperatureCelsius; }
        public void    setTemperatureCelsius(double t) { this.temperatureCelsius = t; }
        public double  getFeelsCelsius()        { return feelsCelsius; }
        public void    setFeelsCelsius(double f){ this.feelsCelsius = f; }
        public double  getHumidity()            { return humidity; }
        public void    setHumidity(double h)    { this.humidity = h; }
        public double  getWindSpeedKmh()        { return windSpeedKmh; }
        public void    setWindSpeedKmh(double w){ this.windSpeedKmh = w; }
        public boolean isHeavyRain()            { return heavyRain; }
        public void    setHeavyRain(boolean b)  { this.heavyRain = b; }
        public boolean isFog()                  { return fog; }
        public void    setFog(boolean f)        { this.fog = f; }
        public String  getForecast()            { return forecast; }
        public void    setForecast(String f)    { this.forecast = f; }

    }

    // ── RouteStatus ───────────────────────────────────────
    public static class RouteStatus {
        private String routeName;
        private String congestionLevel;       // LOW | MEDIUM | HIGH | UNKNOWN
        private double averageSpeedKmh;
        private int    delayMinutes;
        private double distanceKm;
        private double estimatedDurationMin;  // NEW — from FastAPI duration_min
        private String status;                // OPEN | PARTIAL | CLOSED | UNKNOWN
        private String primaryCause;
        private double confidence;            // NEW — from FastAPI confidence

        public String getRouteName()             { return routeName; }
        public void   setRouteName(String n)     { this.routeName = n; }
        public String getCongestionLevel()       { return congestionLevel; }
        public void   setCongestionLevel(String c){ this.congestionLevel = c; }
        public double getAverageSpeedKmh()       { return averageSpeedKmh; }
        public void   setAverageSpeedKmh(double s){ this.averageSpeedKmh = s; }
        public int    getDelayMinutes()          { return delayMinutes; }
        public void   setDelayMinutes(int d)     { this.delayMinutes = d; }
        public double getDistanceKm()            { return distanceKm; }
        public void   setDistanceKm(double d)    { this.distanceKm = d; }
        public double getEstimatedDurationMin()  { return estimatedDurationMin; }
        public void   setEstimatedDurationMin(double d){ this.estimatedDurationMin = d; }
        public String getStatus()                { return status; }
        public void   setStatus(String s)        { this.status = s; }
        public String getPrimaryCause()          { return primaryCause; }
        public void   setPrimaryCause(String c)  { this.primaryCause = c; }
        public double getConfidence()            { return confidence; }
        public void   setConfidence(double c)    { this.confidence = c; }
    }

    // ── Incident ──────────────────────────────────────────
    public static class Incident {
        private String  incidentId;
        private String  title;
        private String  location;
        private String  severity;
        private String  type;
        private String  time;
        private String  affectedRoutes;
        private boolean active;

        public String  getIncidentId()           { return incidentId; }
        public void    setIncidentId(String id)  { this.incidentId = id; }
        public String  getTitle()                { return title; }
        public void    setTitle(String t)        { this.title = t; }
        public String  getLocation()             { return location; }
        public void    setLocation(String l)     { this.location = l; }
        public String  getSeverity()             { return severity; }
        public void    setSeverity(String s)     { this.severity = s; }
        public String  getType()                 { return type; }
        public void    setType(String t)         { this.type = t; }
        public String  getTime()                 { return time; }
        public void    setTime(String t)         { this.time = t; }
        public String  getAffectedRoutes()       { return affectedRoutes; }
        public void    setAffectedRoutes(String r){ this.affectedRoutes = r; }
        public boolean isActive()                { return active; }
        public void    setActive(boolean a)      { this.active = a; }
    }

    // ── EcosystemMetrics ──────────────────────────────────
    public static class EcosystemMetrics {
        private int    healthScore;
        private double co2EmissionsToday;
        private double fuelWastedLiters;
        private double avgFlowEfficiency;
        private String trend;
        private String primaryConcern;

        public int    getHealthScore()            { return healthScore; }
        public void   setHealthScore(int s)       { this.healthScore = s; }
        public double getCo2EmissionsToday()      { return co2EmissionsToday; }
        public void   setCo2EmissionsToday(double c){ this.co2EmissionsToday = c; }
        public double getFuelWastedLiters()       { return fuelWastedLiters; }
        public void   setFuelWastedLiters(double f){ this.fuelWastedLiters = f; }
        public double getAvgFlowEfficiency()      { return avgFlowEfficiency; }
        public void   setAvgFlowEfficiency(double e){ this.avgFlowEfficiency = e; }
        public String getTrend()                  { return trend; }
        public void   setTrend(String t)          { this.trend = t; }
        public String getPrimaryConcern()         { return primaryConcern; }
        public void   setPrimaryConcern(String c) { this.primaryConcern = c; }
    }

    // ── TrafficPrediction ─────────────────────────────────
    public static class TrafficPrediction {
        private String routeName;
        private String timeSlot;
        private String predictedCongestion;
        private double confidenceScore;

        public String getRouteName()                { return routeName; }
        public void   setRouteName(String n)        { this.routeName = n; }
        public String getTimeSlot()                 { return timeSlot; }
        public void   setTimeSlot(String t)         { this.timeSlot = t; }
        public String getPredictedCongestion()      { return predictedCongestion; }
        public void   setPredictedCongestion(String c){ this.predictedCongestion = c; }
        public double getConfidenceScore()          { return confidenceScore; }
        public void   setConfidenceScore(double s)  { this.confidenceScore = s; }
    }

}