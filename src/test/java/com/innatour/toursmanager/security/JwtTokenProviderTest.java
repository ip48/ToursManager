package com.innatour.toursmanager.security;

import com.innatour.toursmanager.config.JwtConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    @Test
    public void generateValidateAndParseToken() {
        JwtConfig cfg = new JwtConfig();
        // 32+ chars secret for HS256
        cfg.setSecret("0123456789ABCDEFGHIJKLMNOPQRSTUVWX");
        cfg.setExpiration(3600_000L);

        JwtTokenProvider provider = new JwtTokenProvider(cfg);

        String email = "test.user@example.com";
        String token = provider.generateToken(email);

        assertNotNull(token, "Token should be generated");
        assertTrue(provider.validateToken(token), "Token should be valid");

        String parsedEmail = provider.getEmailFromToken(token);
        assertEquals(email, parsedEmail, "Parsed email should match original");
    }
}
