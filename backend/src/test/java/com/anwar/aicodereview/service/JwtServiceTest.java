package com.anwar.aicodereview.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            Base64.getEncoder().encodeToString("test-signing-key-that-is-at-least-32-bytes".getBytes()), 3_600_000);

    @Test
    void accessTokenContainsTheSignedUserId() {
        UUID userId = UUID.randomUUID();

        assertEquals(userId, jwtService.extractUserId(jwtService.createAccessToken(userId)));
    }

    @Test
    void tamperedAccessTokenIsRejected() {
        String token = jwtService.createAccessToken(UUID.randomUUID());

        assertThrows(JwtException.class, () -> jwtService.extractUserId(token + "tampered"));
    }
}
