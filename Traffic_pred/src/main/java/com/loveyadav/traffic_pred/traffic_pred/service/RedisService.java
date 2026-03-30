package com.loveyadav.traffic_pred.traffic_pred.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    public void saveToken(String token, long expiryTime){
        redisTemplate.opsForValue().set(token,"BLACKLISTED",expiryTime, TimeUnit.MILLISECONDS);
    }

//    Check if token is blacklisted
    public boolean isBlacklisted(String token){
        return redisTemplate.hasKey(token);
    }
}
