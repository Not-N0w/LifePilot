package com.github.not.n0w.lifepilot.service;

import org.springframework.security.core.userdetails.UserDetails;


public interface JwtService {
    public String generateToken(UserDetails userDetails, Long userId, Boolean isVerified);
    public String extractUsername(String token);
    public boolean isTokenValid(String token, UserDetails userDetails);
    public Long extractUserId(String token);
    public boolean isTokenValid(String token);
    public String generateRefreshToken(UserDetails userDetails, Long userId);
    public String generateVerificationToken(String email, int code);
    public String validateVerificationCode(String token, int userInputCode);
    public boolean extractIsVerified(String token);
}
