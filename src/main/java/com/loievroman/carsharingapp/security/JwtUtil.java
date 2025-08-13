package com.loievroman.carsharingapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretKey secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secret)
                .compact();

    }

    public boolean isValidToken(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token);
        return !claimsJws.getPayload().getExpiration().before(new Date());
    }

    public String getUsername(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token);
        return claimsJws.getPayload().getSubject();
    }
}
