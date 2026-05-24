//package com.loveyadav.traffic_pred.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.security.SecureRandom;
//import java.time.Duration;
//
///**
// * Generates 6-digit OTPs, stores them in Redis with TTL,
// * and verifies them. Keys are namespaced so email and phone
// * OTPs never collide.
// *
// *  Redis key format:
// *    otp:email:<userEmail>:<targetEmail>   → email-change OTP
// *    otp:phone:<userEmail>                 → phone-verify OTP
// */
//@Service
//public class OtpService {
//
//    private static final SecureRandom RANDOM = new SecureRandom();
//
//    @Autowired
//    private StringRedisTemplate redis;
//
//    @Value("${otp.ttl-seconds:300}")
//    private long ttlSeconds;
//
//    // ── Generate & store ──────────────────────────────
//
//    public String generateEmailOtp(String userEmail, String targetEmail) {
//        String otp = sixDigits();
//        String key = emailKey(userEmail, targetEmail);
//        redis.opsForValue().set(key, otp, Duration.ofSeconds(ttlSeconds));
//        return otp;
//    }
//
//    public String generatePhoneOtp(String userEmail) {
//        String otp = sixDigits();
//        redis.opsForValue().set(phoneKey(userEmail), otp, Duration.ofSeconds(ttlSeconds));
//        return otp;
//    }
//
//    // ── Verify & delete (one-time use) ────────────────
//
//    public boolean verifyEmailOtp(String userEmail, String targetEmail, String otp) {
//        String key = emailKey(userEmail, targetEmail);
//        String stored = redis.opsForValue().get(key);
//        if (stored != null && stored.equals(otp)) {
//            redis.delete(key);   // one-time use
//            return true;
//        }
//        return false;
//    }
//
//    public boolean verifyPhoneOtp(String userEmail, String otp) {
//        String key = phoneKey(userEmail);
//        String stored = redis.opsForValue().get(key);
//        if (stored != null && stored.equals(otp)) {
//            redis.delete(key);
//            return true;
//        }
//        return false;
//    }
//
//    // ── Helpers ───────────────────────────────────────
//
//    private String sixDigits() {
//        return String.format("%06d", RANDOM.nextInt(1_000_000));
//    }
//
//    private String emailKey(String userEmail, String targetEmail) {
//        return "otp:email:" + userEmail + ":" + targetEmail;
//    }
//
//    private String phoneKey(String userEmail) {
//        return "otp:phone:" + userEmail;
//    }
//
//    public long getTtlSeconds() {
//        return ttlSeconds;
//    }
//}