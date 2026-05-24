package com.loveyadav.traffic_pred.service;

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.*;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class FastApiAuthService {
//
//    @Value("${fastapi.login-url}")
//    private String LOGIN_URL;
//    private String token;
//    private long expiryTime;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
////    public String getToken(){
////        if(token != null && expiryTime > System.currentTimeMillis()) return token;
////        Map<String,String> body = new HashMap<>();
////        body.put("username","admin");
////        body.put("password","admin123");
////        HttpHeaders httpHeaders = new HttpHeaders();
////        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
////        HttpEntity<Map<String,String>> request = new HttpEntity<>(body,httpHeaders);
////        ResponseEntity<Map> response = restTemplate.postForEntity(LOGIN_URL, request, Map.class);
////        token = (String) response.getBody().get("access_token");
////        expiryTime = System.currentTimeMillis() + (2*60*60*1000);
////        System.out.println("FASTAPI TOKEN: " + token);
////        return token;
////    }
////    public String getToken() {
////
////        // ✅ FIX expiry condition (you wrote wrong earlier)
////        if (token != null && expiryTime > System.currentTimeMillis()) {
////            return token;
////        }
////
////
////        try {
////            Map<String, String> body = new HashMap<>();
////            body.put("username", "admin");
////            body.put("password", "admin123");
////
////            HttpHeaders headers = new HttpHeaders();
////            headers.setContentType(MediaType.APPLICATION_JSON);
////
////            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
////
////            ResponseEntity<Map> response = restTemplate.postForEntity(
////                    LOGIN_URL,
////                    request,
////                    Map.class
////            );
////
////            System.out.println("FASTAPI LOGIN RESPONSE: " + response.getBody());
////
////            // ✅ CORRECT extraction
////            String newToken = (String) response.getBody().get("access_token");
////
////            if (newToken == null) {
////                throw new RuntimeException("FastAPI token is null!");
////            }
////
////            System.out.println("LOGIN URL: " + LOGIN_URL);
////            System.out.println("TOKEN RECEIVED: " + newToken);
////            this.token = newToken;
////            this.expiryTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
////
////            return newToken;
////
////        } catch (Exception e) {
////            e.printStackTrace();
////            throw new RuntimeException("FastAPI login failed: " + e.getMessage());
////        }
////    }
//    public String getToken() {
//        try {
//            if (token != null && expiryTime > System.currentTimeMillis()) {
//                return token;
//            }
//
//            Map<String, String> body = new HashMap<>();
//            body.put("username", "admin");
//            body.put("password", "admin123");
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(LOGIN_URL, request, Map.class);
//
//            // ✅ CORRECT EXTRACTION
//            String newToken = (String) response.getBody().get("access_token");
//
//            if (newToken == null) {
//                throw new RuntimeException("FastAPI token is null!");
//            }
//
//            this.token = newToken;
//            this.expiryTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
//
//            return token;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("FastAPI login failed: " + e.getMessage());
//        }
//    }
//}



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FastApiAuthService {

    private static final Logger log = LoggerFactory.getLogger(FastApiAuthService.class);

    // Token expires 2 hours from issue; we refresh 5 minutes early (buffer)
    private static final long TOKEN_VALIDITY_MS = (2 * 60 * 60 * 1000) - (5 * 60 * 1000);

    @Value("${fastapi.login-url}")
    private String loginUrl;

    @Value("${fastapi.username}")
    private String username;

    @Value("${fastapi.password}")
    private String password;

    private final RestTemplate restTemplate;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile String cachedToken;
    private volatile long expiryTime;

    public FastApiAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getToken() {
        // Fast path — no lock needed if token is still valid
        if (isTokenValid()) {
            return cachedToken;
        }

        // Slow path — only one thread fetches a new token
        lock.lock();
        try {
            // Double-check after acquiring lock
            // (another thread may have refreshed it while we waited)
            if (isTokenValid()) {
                return cachedToken;
            }

            return fetchNewToken();

        } finally {
            lock.unlock();
        }
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────

    private boolean isTokenValid() {
        return cachedToken != null && System.currentTimeMillis() < expiryTime;
    }

    private String fetchNewToken() {
        log.info("Fetching new FastAPI token from {}", loginUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "username", username,
                "password", password
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    loginUrl, request, Map.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("FastAPI login returned empty body");
            }

            String newToken = (String) response.getBody().get("access_token");

            if (newToken == null || newToken.isBlank()) {
                throw new RuntimeException("FastAPI returned null/blank access_token");
            }

            this.cachedToken = newToken;
            this.expiryTime = System.currentTimeMillis() + TOKEN_VALIDITY_MS;

            log.info("FastAPI token refreshed successfully. Expires in ~115 minutes.");
            return cachedToken;

        } catch (Exception e) {
            log.error("FastAPI login failed: {}", e.getMessage());
            throw new RuntimeException("FastAPI authentication failed: " + e.getMessage(), e);
        }
    }
}