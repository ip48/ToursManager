package com.innatour.toursmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties
 * 
 * Maps jwt.* properties from application.properties to this class
 * This tells Spring Boot these are valid properties
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT secret key for signing tokens (minimum 32 characters for HS256)
     */
    private String secret;
    
    /**
     * Token expiration time in milliseconds (default: 24 hours)
     */
    private long expiration;
    
    // Getters and Setters
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
