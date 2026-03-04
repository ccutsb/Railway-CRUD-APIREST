package com.empresaccutsb.apirest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresaccutsb.apirest.config.JwtProperties;
import com.empresaccutsb.apirest.dto.auth.LoginRequest;
import com.empresaccutsb.apirest.dto.auth.RefreshRequest;
import com.empresaccutsb.apirest.dto.auth.RegisterRequest;
import com.empresaccutsb.apirest.exception.ConflictException;
import com.empresaccutsb.apirest.exception.UnauthorizedException;
import com.empresaccutsb.apirest.model.AppUser;
import com.empresaccutsb.apirest.model.RefreshToken;
import com.empresaccutsb.apirest.repository.AppUserRepository;
import com.empresaccutsb.apirest.repository.RefreshTokenRepository;
import com.empresaccutsb.apirest.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private AuditLogService auditLogService;

    private JwtProperties jwtProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setRefreshTokenDays(7);
        authService =
                new AuthService(
                        appUserRepository,
                        refreshTokenRepository,
                        passwordEncoder,
                        authenticationManager,
                        jwtService,
                        jwtProperties,
                        auditLogService);
    }

    @Test
    void registerShouldCreateUserAndTokens() {
        RegisterRequest request = new RegisterRequest("newuser", "new@mail.com", "Password1");
        when(passwordEncoder.encode("Password1")).thenReturn("hash");
        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(
                        invocation -> {
                            AppUser user = invocation.getArgument(0);
                            ReflectionTestUtils.setField(user, "id", 11L);
                            return user;
                        });
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);

        var response = authService.register(request, "127.0.0.1");

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void registerShouldFailWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("newuser", "new@mail.com", "Password1");
        when(appUserRepository.existsByUsername("newuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void loginShouldFailWhenUserMissing() {
        LoginRequest request = new LoginRequest("ghost", "Password1");
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(UnauthorizedException.class);

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void refreshShouldFailWhenExpired() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("r1");
        refreshToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByTokenAndRevokedFalse("r1"))
                .thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("r1"), "127.0.0.1"))
                .isInstanceOf(UnauthorizedException.class);
    }
}
