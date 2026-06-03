package com.loveyadav.traffic_pred.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "traffic_record")
public class TrafficRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // ← lowercase 'id' so Jackson serializes as "id" not "Id"

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore               // ← don't expose full User object in API responses
    private User user;

    private String source;
    private String destination;

    private int predictedTraffic;
    private double confidence;

    @JsonProperty("distance_km")
    private double distanceKm;

    @JsonProperty("duration_min")
    private double durationMin;
    @Column(name = "created_at")
    private LocalDateTime timestamp;
}