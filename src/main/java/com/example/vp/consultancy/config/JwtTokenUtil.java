package com.example.vp.consultancy.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    private final Key key;
    private final long jwtExpirationMs;

    public JwtTokenUtil(@Value("${app.jwt.secret}") String secret,
                        @Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        logger.info("Generating token for subject: {}, with claims: {}, issued at: {}, expires at: {}", subject, claims, now, expiry);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        logger.info("Validating token: {}", token);
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.info("Token is valid: {}", token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("Invalid token: {}", token, ex);
            return false;
        }
    }

    public Claims getClaims(String token) {
        logger.info("Extracting claims from token: {}", token);
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}