package com.empresaccutsb.apirest.controller;

import com.empresaccutsb.apirest.dto.auth.AuthResponse;
import com.empresaccutsb.apirest.dto.auth.LoginRequest;
import com.empresaccutsb.apirest.dto.auth.LogoutRequest;
import com.empresaccutsb.apirest.dto.auth.RefreshRequest;
import com.empresaccutsb.apirest.dto.auth.RegisterRequest;
import com.empresaccutsb.apirest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Registro, login y manejo de tokens")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuario",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Usuario registrado",
                        content =
                                @Content(
                                        schema = @Schema(implementation = AuthResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                "{\"accessToken\":\"...\",\"refreshToken\":\"...\",\"tokenType\":\"Bearer\",\"expiresInSeconds\":900}")))
            })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request, httpServletRequest.getRemoteAddr()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotar refresh token")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.refresh(request, httpServletRequest.getRemoteAddr()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidar refresh token")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {
        String actor = authentication == null ? "anonymous" : authentication.getName();
        authService.logout(request.refreshToken(), actor, httpServletRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
