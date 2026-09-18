package com.karthik.incidentmanagement.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // @Value fields aren't populated outside a Spring context, so they're
        // set directly here — this is a plain unit test, no @SpringBootTest.
        ReflectionTestUtils.setField(jwtService, "secret", "unit-test-secret-key-must-be-at-least-32-bytes-long");
        ReflectionTestUtils.setField(jwtService, "expiration", 60_000L);
    }

    @Test
    void generateToken_thenExtractUsername_roundTrips() {
        String token = jwtService.generateToken("user@example.com");

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
    }

    @Test
    void validateToken_returnsTrue_forMatchingEmail() {
        String token = jwtService.generateToken("user@example.com");

        assertThat(jwtService.validateToken(token, "user@example.com")).isTrue();
    }

    @Test
    void validateToken_returnsFalse_forDifferentEmail() {
        String token = jwtService.generateToken("user@example.com");

        assertThat(jwtService.validateToken(token, "someone-else@example.com")).isFalse();
    }

    @Test
    void extractUsername_rejectsExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L); // already expired

        String expiredToken = jwtService.generateToken("user@example.com");

        assertThatThrownBy(() -> jwtService.extractUsername(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
