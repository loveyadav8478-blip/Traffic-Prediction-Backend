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
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TrafficService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TrafficRepository trafficRepository;
    @Autowired
    private FastApiAuthService fastApiAuthService;

    @Value("${fastapi.predict-url}")
    private String PREDICT_URL;

    public Object getTrafficPrediction(RouteRequest req) {
        try {
            log.info("Calling FastAPI from {} -> {}", req.getSource(), req.getDestination());

            //Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", "aa5d066ad3983b27133902f4931636477d7df15ac15188a756e7b9afa46fd35e");
            String token = fastApiAuthService.getToken();
            System.out.println("TOKEN : "+token);
            headers.setBearerAuth(token);

            //Convert request to JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(req);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PREDICT_URL,request,String.class);

            String responseBody = responseEntity.getBody();
            log.info("FASTAPI RESPONSE: {}", responseBody);
            //Convert JSON → Map
            Map<String, Object> map = mapper.readValue(responseBody, Map.class);
            //Validate response
            Object bestRouteObj = map.get("best_route");
            if (bestRouteObj == null) {
                throw new RuntimeException("best_route missing in response → " + responseBody);
            }
            Map<String, Object> bestRoute = (Map<String, Object>) bestRouteObj;
            //Extract values safely
            int prediction = ((Number) bestRoute.get("final_prediction")).intValue();
            double confidence = ((Number) bestRoute.get("confidence")).doubleValue();
            double distance = ((Number) bestRoute.get("distance_km")).doubleValue();
            double duration = ((Number) bestRoute.get("duration_min")).doubleValue();
            //Save to DB
            TrafficRecord record = new TrafficRecord();
            record.setSource(req.getSource());
            record.setDestination(req.getDestination());
            record.setPredictedTraffic(prediction);
            record.setConfidence(confidence);
            record.setDistanceKm(distance);
            record.setDurationMin(duration);
            record.setTimestamp(LocalDateTime.now());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
            record.setUser(user);
            trafficRepository.save(record);
            return map;

        } catch (Exception e) {
            log.error("!!ERROR while calling FastAPI", e);
            throw new RuntimeException("Prediction failed: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteByUserId(Long id, String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User Not Found!!"));
        TrafficRecord record = trafficRepository.findById(id).orElseThrow(() -> new RuntimeException("Record not found"));

        if(!record.getUser().getId().equals(user.getId())){
            throw new RuntimeException("Unauthorized");
        }
        trafficRepository.delete(record);
    }

    @Transactional
    public void deleteAllRecords(){
        trafficRepository.deleteAll();
    }

}

