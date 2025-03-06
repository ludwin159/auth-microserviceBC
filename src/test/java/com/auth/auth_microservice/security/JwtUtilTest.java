package com.auth.auth_microservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkey";
        jwtUtil = new JwtUtil(SECRET_KEY);
    }

    @Test
    @DisplayName("Generate token")
    void testGenerateToken() {
        String username = "USER_001";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Extract username when token is valid")
    void testExtractUsername() {
        String username = "USER_001";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Validate when token is correct")
    void testValidateToken() {
        String username = "USER_001";
        String token = jwtUtil.generateToken(username);

        assertTrue(jwtUtil.validateToken(token, username));
    }

    @Test
    @DisplayName("Detect token expired")
    void testIsTokenExpired() {
        String expiredToken = Jwts.builder()
                .setSubject("USER_001")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(jwtUtil.key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        assertTrue(jwtUtil.isTokenExpired(expiredToken));
    }

    @Test
    @DisplayName("Validate incorrect token")
    void testInvalidToken() {
        String username = "USER_001";
        String token = jwtUtil.generateToken(username);

        assertFalse(jwtUtil.validateToken(token, "otherUser")); // Usuario diferente
    }

    @Test
    @DisplayName("extract claims from a token")
    void testExtractClaim() {
        String username = "USER_001";
        String token = jwtUtil.generateToken(username);

        Function<Claims, String> claimResolver = Claims::getSubject;
        String extractedClaim = jwtUtil.extractClaim(token, claimResolver);

        assertEquals(username, extractedClaim);
    }
}