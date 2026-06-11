package com.loveyadav.traffic_pred.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;   // null for Google-only users

    @Enumerated(EnumType.STRING)
    private Role role;

    // ── Google OAuth fields ───────────────────────────
    private String googleId;   // Google's "sub" field — unique per user
    private String picture;    // Google profile picture URL

}