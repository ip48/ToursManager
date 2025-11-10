package com.innatour.toursmanager.security;

import com.innatour.toursmanager.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider - Generates and validates JWT tokens
 * 
 * JWT Structure: header.payload.signature
 * - Header: algorithm + token type
 * - Payload: claims (user data)
 * - Signature: ensures token hasn't been tampered with
 */
@Component
public class JwtTokenProvider {
    
    private final JwtConfig jwtConfig;
    
    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }
    
    /**
     * Generate JWT token from email
     * Token contains: email, issued time, expiration time
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
        
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(email)  // Subject = user identifier (email in our case)
                .issuedAt(now)   // When token was created
                .expiration(expiryDate)  // When token expires
                .signWith(key)   // Sign with secret key
                .compact();      // Build the JWT string
    }
    
    /**
     * Extract email from JWT token
     */
    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();  // Returns the email
    }
    
    /**
     * Validate JWT token
     * Returns true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
            
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid (expired, malformed, wrong signature, etc.)
            return false;
        }
    }
}
