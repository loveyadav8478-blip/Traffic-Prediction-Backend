package com.loveyadav.traffic_pred.traffic_pred.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class FastApiAuthService {

    @Value("${fastapi.login-url}")
    private String LOGIN_URL;
    private String token;
    private long expiryTime;

    @Autowired
    private RestTemplate restTemplate;
    public String getToken(){
        if(token != null && expiryTime < System.currentTimeMillis()){
            return token;
        }
        Map<String,String> body = new HashMap<>();
        body.put("username","admin");
        body.put("password","admin123");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String,String>> request = new HttpEntity<>(body,httpHeaders);
        ResponseEntity<Map> response = restTemplate.postForEntity(LOGIN_URL, request, Map.class);
        response.getBody().get("access_token");
        String temptoken = response.getBody().toString();
        String token = response.getBody().toString().substring(14,temptoken.length()-1);
        expiryTime = System.currentTimeMillis() + (2*60*60*1000);
        System.out.println("Token : "+ token);
        return token;
    }
}
