//package com.loveyadav.traffic_pred.service;
//
//import com.loveyadav.traffic_pred.dto.RouteRequest;
//import com.loveyadav.traffic_pred.entity.TrafficRecord;
//import com.loveyadav.traffic_pred.entity.User;
//import com.loveyadav.traffic_pred.repository.TrafficRepository;
//import com.loveyadav.traffic_pred.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//@Slf4j
//public class TrafficService {
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private RestTemplate restTemplate;
//    @Autowired
//    private TrafficRepository trafficRepository;
//    @Autowired
//    private FastApiAuthService fastApiAuthService;
//
//    @Value("${fastapi.predict-url}")
//    private String PREDICT_URL;
//
//    public Object getTrafficPrediction(RouteRequest req) {
//        try {
//            log.info("Calling FastAPI from {} -> {}", req.getSource(), req.getDestination());
//
//            //Headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("x-api-key", "aa5d066ad3983b27133902f4931636477d7df15ac15188a756e7b9afa46fd35e");
//            String token = fastApiAuthService.getToken();
//            System.out.println("TOKEN : "+token);
//            headers.setBearerAuth(token);
//
//            //Convert request to JSON
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonBody = mapper.writeValueAsString(req);
//            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
//            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PREDICT_URL,request,String.class);
//
//            String responseBody = responseEntity.getBody();
//            log.info("FASTAPI RESPONSE: {}", responseBody);
//            //Convert JSON → Map
//            Map<String, Object> map = mapper.readValue(responseBody, Map.class);
//            //Validate response
//            Object bestRouteObj = map.get("best_route");
//            if (bestRouteObj == null) {
//                throw new RuntimeException("best_route missing in response → " + responseBody);
//            }
//            Map<String, Object> bestRoute = (Map<String, Object>) bestRouteObj;
//            //Extract values safely
//            int prediction = ((Number) bestRoute.get("final_prediction")).intValue();
//            double confidence = ((Number) bestRoute.get("confidence")).doubleValue();
//            double distance = ((Number) bestRoute.get("distance_km")).doubleValue();
//            double duration = ((Number) bestRoute.get("duration_min")).doubleValue();
//            //Save to DB
//            TrafficRecord record = new TrafficRecord();
//            record.setSource(req.getSource());
//            record.setDestination(req.getDestination());
//            record.setPredictedTraffic(prediction);
//            record.setConfidence(confidence);
//            record.setDistanceKm(distance);
//            record.setDurationMin(duration);
//            record.setTimestamp(LocalDateTime.now());
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String email = authentication.getName();
//            User user = userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
//            record.setUser(user);
//            trafficRepository.save(record);
//            return map;
//
//        } catch (Exception e) {
//            log.error("!!ERROR while calling FastAPI", e);
//            throw new RuntimeException("Prediction failed: " + e.getMessage());
//        }
//    }
//
//    @Transactional
//    public void deleteByUserId(Long id, String email){
//        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User Not Found!!"));
//        TrafficRecord record = trafficRepository.findById(id).orElseThrow(() -> new RuntimeException("Record not found"));
//
//        if(!record.getUser().getId().equals(user.getId())){
//            throw new RuntimeException("Unauthorized");
//        }
//        trafficRepository.delete(record);
//    }
//
//    @Transactional
//    public void deleteAllRecords(){
//        trafficRepository.deleteAll();
//    }
//
//}
//

package com.loveyadav.traffic_pred.service;

import com.loveyadav.traffic_pred.dto.RouteRequest;
import com.loveyadav.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TrafficService {

    @Autowired private UserRepository userRepository;
    @Autowired private RestTemplate restTemplate;
    @Autowired private TrafficRepository trafficRepository;
    @Autowired private FastApiAuthService fastApiAuthService;

    @Value("${fastapi.predict-url}")
    private String PREDICT_URL;

    @Transactional
    public Object getTrafficPrediction(RouteRequest req) {
        try {
            log.info("Calling FastAPI from {} -> {}", req.getSource(), req.getDestination());

            // ── Headers ──────────────────────────────────────────────
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", "aa5d066ad3983b27133902f4931636477d7df15ac15188a756e7b9afa46fd35e");
            String token = fastApiAuthService.getToken();
            System.out.println("TOKEN : " + token);
            headers.setBearerAuth(token);

            // ── Build enriched body with ALL features ─────────────────
            Map<String, Object> body = new HashMap<>();
            body.put("source",      req.getSource());
            body.put("destination", req.getDestination());

//            // Hour — parse from "HH:mm" or fall back to current hour
//            int hour = LocalDateTime.now().getHour();
//            if (req.getTime() != null && req.getTime().contains(":")) {
//                try { hour = Integer.parseInt(req.getTime().split(":")[0]); } catch (Exception ignored) {}
//            }
            // FIXED — handles both "19:00" and "07:00 PM"
            int hour = LocalDateTime.now().getHour();
            if (req.getTime() != null && req.getTime().contains(":")) {
                try {
                    String t = req.getTime().trim().toUpperCase();
                    int parsed = Integer.parseInt(t.split(":")[0].trim());
                    if (t.contains("PM") && parsed != 12) parsed += 12;
                    if (t.contains("AM") && parsed == 12) parsed = 0;
                    hour = parsed;
                } catch (Exception ignored) {}
            }
            body.put("hour", hour);

            // Day of week — Monday=0 … Sunday=6
            int dow = LocalDateTime.now().getDayOfWeek().getValue() - 1;
            if (req.getDayOfWeek() != null && !req.getDayOfWeek().isBlank()) {
                String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
                for (int i = 0; i < days.length; i++) {
                    if (days[i].equalsIgnoreCase(req.getDayOfWeek())) { dow = i; break; }
                }
            }
            body.put("day_of_week", dow);
            body.put("is_weekend",  (dow >= 5) ? 1 : 0);
            body.put("is_peak_hour", ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 20)) ? 1 : 0);

            // Weather — map frontend label → FastAPI expected value
            String rawWeather = req.getWeatherCondition() != null ? req.getWeatherCondition() : "Clear";
            String weather = switch (rawWeather.toLowerCase()) {
                case "rainy"        -> "Rain";
                case "cloudy"       -> "Clouds";
                case "foggy"        -> "Fog";
                case "snowy"        -> "Snow";
                case "thunderstorm" -> "Thunderstorm";
                case "haze"         -> "Haze";
                case "windy"        -> "Clouds";
                case "sandstorm"    -> "Haze";
                default             -> "Clear";
            };
            body.put("weather", weather);

            // Optional distance override
            if (req.getDistance() != null && req.getDistance() > 0) {
                body.put("distance_km", req.getDistance());
            }

            // ── Call FastAPI ──────────────────────────────────────────
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PREDICT_URL, request, String.class);

            String responseBody = responseEntity.getBody();
            log.info("FASTAPI RESPONSE: {}", responseBody);

            Map<String, Object> map = mapper.readValue(responseBody, Map.class);
            Object bestRouteObj = map.get("best_route");
            if (bestRouteObj == null) throw new RuntimeException("best_route missing → " + responseBody);

            @SuppressWarnings("unchecked")
            Map<String, Object> bestRoute = (Map<String, Object>) bestRouteObj;

            int    finalPrediction = ((Number) bestRoute.get("final_prediction")).intValue();
            double confidence      = ((Number) bestRoute.get("confidence")).doubleValue();
            double distance        = ((Number) bestRoute.get("distance_km")).doubleValue();
            double duration        = ((Number) bestRoute.get("duration_min")).doubleValue();

            // traffic_label is a string like "Low" / "Medium" / "High"
            String trafficLabel = bestRoute.containsKey("traffic_label")
                    ? (String) bestRoute.get("traffic_label")
                    : labelFromInt(finalPrediction);

            // ── Save to DB ────────────────────────────────────────────
            TrafficRecord record = new TrafficRecord();
            record.setSource(req.getSource());
            record.setDestination(req.getDestination());
            record.setPredictedTraffic(finalPrediction);
            record.setConfidence(confidence);
            record.setDistanceKm(distance);
            record.setDurationMin(duration);
            record.setTimestamp(LocalDateTime.now());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            record.setUser(user);
            trafficRepository.save(record);

            // ── Inject traffic_label into response so frontend gets string ──
            bestRoute.put("traffic_label", trafficLabel);

            return map;

        } catch (Exception e) {
            log.error("!!ERROR while calling FastAPI", e);
            throw new RuntimeException("Prediction failed: " + e.getMessage());
        }
    }

    private String labelFromInt(int val) {
        return switch (val) {
            case 0  -> "Low";
            case 1  -> "Medium";
            case 2  -> "High";
            default -> "Low";
        };
    }

    @Transactional
    public void deleteByUserId(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found!!"));
        TrafficRecord record = trafficRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        if (!record.getUser().getId().equals(user.getId())) throw new RuntimeException("Unauthorized");
        trafficRepository.delete(record);
    }

    @Transactional
    public void deleteAllRecords() { trafficRepository.deleteAll(); }
}