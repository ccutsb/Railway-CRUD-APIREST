package com.empresaccutsb.apirest.dto.auth;

public record AuthResponse(
        String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}
