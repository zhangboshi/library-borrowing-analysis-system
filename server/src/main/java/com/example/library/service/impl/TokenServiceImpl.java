package com.example.library.service.impl;

import com.example.library.service.TokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenServiceImpl implements TokenService {

    private final StringRedisTemplate redisTemplate;

    public TokenServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void storeToken(String token, String username, long expireHours) {
        redisTemplate.opsForValue().set(buildKey(token), username, Duration.ofHours(expireHours));
    }

    @Override
    public boolean isTokenValid(String token) {
        Boolean exists = redisTemplate.hasKey(buildKey(token));
        return exists != null && exists;
    }

    @Override
    public void invalidateToken(String token) {
        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return "auth:token:" + token;
    }
}
