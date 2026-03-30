package com.loveyadav.traffic_pred.traffic_pred.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class TrafficRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    private String source;
    private String destination;

    private int predictedTraffic;
    private double confidence;

    private double distanceKm;
    private double durationMin;

    private LocalDateTime timestamp;

}
