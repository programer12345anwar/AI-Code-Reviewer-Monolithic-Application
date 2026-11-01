package com.anwar.aicodereview.dto;

import java.util.UUID;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, UUID userId) {
}
