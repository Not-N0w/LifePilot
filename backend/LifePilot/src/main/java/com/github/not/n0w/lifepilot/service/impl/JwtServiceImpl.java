package com.github.not.n0w.lifepilot.service.impl;

import com.github.not.n0w.lifepilot.config.JwtProperties;
import com.github.not.n0w.lifepilot.config.SecurityConfig;
import com.github.not.n0w.lifepilot.exception.TokenExpiredException;
import com.github.not.n0w.lifepilot.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtProperties jwtProperties;
    private static final long REFRESH_TOKEN_EXP = 1000L * 60 * 60 * 24 * 7; // 7 дней

    @Override
    public String generateToken(UserDetails userDetails, Long userId) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 час
                .claim("userId", userId)
                .signWith(getSignKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP))
                .claim("userId", userId)
                .signWith(getSignKey())
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (!extractUsername(token).equals(userDetails.getUsername())) {
            throw new AuthenticationException("Invalid token: username does not match") {};
        }
        if (isTokenExpired(token)) {
            throw new TokenExpiredException("Token has expired") {};
        }
        return true;
    }

    @Override
    public boolean isTokenValid(String token) {
        if (isTokenExpired(token)) {
            throw new TokenExpiredException("Token has expired") {};
        }
        return true;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new AuthenticationException("Invalid token format") {};
        }
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
