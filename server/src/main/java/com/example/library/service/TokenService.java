package com.example.library.service;

public interface TokenService {

    void storeToken(String token, String username, long expireHours);

    boolean isTokenValid(String token);

    void invalidateToken(String token);
}
