package com.empresaccutsb.apirest.service;

import com.empresaccutsb.apirest.config.JwtProperties;
import com.empresaccutsb.apirest.dto.auth.AuthResponse;
import com.empresaccutsb.apirest.dto.auth.LoginRequest;
import com.empresaccutsb.apirest.dto.auth.RefreshRequest;
import com.empresaccutsb.apirest.dto.auth.RegisterRequest;
import com.empresaccutsb.apirest.exception.ConflictException;
import com.empresaccutsb.apirest.exception.UnauthorizedException;
import com.empresaccutsb.apirest.model.AppUser;
import com.empresaccutsb.apirest.model.RefreshToken;
import com.empresaccutsb.apirest.model.Role;
import com.empresaccutsb.apirest.repository.AppUserRepository;
import com.empresaccutsb.apirest.repository.RefreshTokenRepository;
import com.empresaccutsb.apirest.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuditLogService auditLogService;

    public AuthService(
            AppUserRepository appUserRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            AuditLogService auditLogService) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ip) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new ConflictException("El nombre de usuario ya existe");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya existe");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.ROLE_USER));
        appUserRepository.save(user);

        auditLogService.log("REGISTER", "USER", user.getId().toString(), user.getUsername(), true, ip);
        return generateTokens(user, ip);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ip) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        AppUser user =
                appUserRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new UnauthorizedException("Credenciales invalidas"));

        auditLogService.log("LOGIN", "USER", user.getId().toString(), user.getUsername(), true, ip);
        return generateTokens(user, ip);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request, String ip) {
        RefreshToken storedToken =
                refreshTokenRepository
                        .findByTokenAndRevokedFalse(request.refreshToken())
                        .orElseThrow(() -> new UnauthorizedException("Refresh token invalido"));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new UnauthorizedException("Refresh token expirado");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        return generateTokens(storedToken.getUser(), ip);
    }

    @Transactional
    public void logout(String refreshToken, String actor, String ip) {
        refreshTokenRepository
                .findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(
                        token -> {
                            token.setRevoked(true);
                            refreshTokenRepository.save(token);
                        });
        auditLogService.log("LOGOUT", "USER", actor, actor, true, ip);
    }

    private AuthResponse generateTokens(AppUser user, String ip) {
        String accessToken =
                jwtService.generateAccessToken(
                        user.getUsername(),
                        Map.of("roles", user.getRoles().stream().map(Enum::name).toList()));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setCreatedByIp(ip);
        refreshToken.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenDays(), ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken, refreshToken.getToken(), "Bearer", jwtService.accessTokenTtlSeconds());
    }
}
