package com.loveyadav.traffic_pred.traffic_pred.service;

import com.loveyadav.traffic_pred.traffic_pred.dto.RouteRequest;
import com.loveyadav.traffic_pred.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.traffic_pred.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

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
            headers.set("x-api-key", "ebb59d0e68e57ce523c02244343403eed9af45179892dc641d134ab43e8e2261");
            String token = fastApiAuthService.getToken();
            System.out.println("TOKEN : "+token);
            headers.setBearerAuth(token);

            //Convert request to JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(req);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            //Call FastAPI
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    PREDICT_URL,
                    request,
                    String.class
            );

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
            double duration = ((Number) bestRoute.get("real_duration_min")).doubleValue();

            //Save to DB
            TrafficRecord record = new TrafficRecord();
            record.setSource(req.getSource());
            record.setDestination(req.getDestination());
            record.setPredictedTraffic(prediction);
            record.setConfidence(confidence);
            record.setDistanceKm(distance);
            record.setDurationMin(duration);
            record.setTimestamp(LocalDateTime.now());

            if (req.getUserId() != null) {
                User user = userRepository.findById(req.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
                record.setUser(user);
            }

            trafficRepository.save(record);
            return map;

        } catch (Exception e) {
            log.error("!!ERROR while calling FastAPI", e);
            throw new RuntimeException("Prediction failed: " + e.getMessage());
        }
    }
}