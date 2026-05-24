//package com.loveyadav.traffic_pred.traffic_pred.config;
////
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////
////@Configuration
////public class CorsConfig {
////
////    @Bean
////    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
////        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
////
////        config.setAllowedOrigins(java.util.List.of("http://localhost:5174")); // frontend
////        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
////        config.setAllowedHeaders(java.util.List.of("*"));
////        config.setAllowCredentials(true);
////
////        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
////                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
////
////        source.registerCorsConfiguration("/**", config);
////        return source;
////    }
////}
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.*;
//
//import java.util.List;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//
//        // ⚠️ MUST match frontend EXACTLY
//        config.setAllowedOrigins(List.of(
//                "http://localhost:5173",
//                "http://localhost:5174"
//        ));
//
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//
//        config.setAllowedHeaders(List.of("*"));
//
//        config.setAllowCredentials(true);
//
//        // 🔥 IMPORTANT for Authorization header
//        config.setExposedHeaders(List.of("Authorization"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        return source;
//    }
//}