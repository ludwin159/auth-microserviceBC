package com.auth.auth_microservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private WebFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        filterChain = mock(WebFilterChain.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
    }

    @Test
    @DisplayName("Permit the request when not have a token")
    void testFilterWithoutToken() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/")
                .build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(jwtUtil, never()).extractUsername(anyString());
        verify(jwtUtil, never()).isTokenExpired(anyString());
    }

    @Test
    @DisplayName("Correct authenticate when the token is valid")
    void testFilterWithValidToken() {
        String validToken = "valid.jwt.token";
        String username = "testUser";

        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build());

        when(jwtUtil.extractUsername(validToken)).thenReturn(username);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(false);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(jwtUtil, times(1)).isTokenExpired(validToken);
    }

    @Test
    @DisplayName("Not authenticate when the token is invalid")
    void testFilterWithInvalidToken() {
        String invalidToken = "invalid.jwt.token";

        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build());

        when(jwtUtil.extractUsername(invalidToken)).thenReturn(null);
        when(jwtUtil.isTokenExpired(invalidToken)).thenReturn(true);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(jwtUtil, times(1)).extractUsername(invalidToken);
    }
}