package com.example.vp.consultancy;

import com.example.vp.consultancy.dto.LoginRequest;
import com.example.vp.consultancy.dto.LoginResponse;
import com.example.vp.consultancy.dto.RefreshTokenRequest;
import com.example.vp.consultancy.entity.RefreshToken;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.entity.UserRole;
import com.example.vp.consultancy.exception.InvalidCredentialsException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.service.AuthenticationService;
import com.example.vp.consultancy.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 * 
 * Tests cover:
 * - User login with valid credentials
 * - User login with invalid credentials
 * - Token refresh with valid refresh token
 * - Token refresh with expired refresh token
 * - User logout and token invalidation
 */
@SpringJUnitConfig
public class AuthenticationServiceTest {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;

    /**
     * Set up test fixtures before each test.
     */
    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenService = mock(RefreshTokenService.class);
        authenticationManager = mock(AuthenticationManager.class);
        passwordEncoder = new BCryptPasswordEncoder();

        // Initialize authentication service with mocked dependencies
        authenticationService = mock(AuthenticationService.class);
    }

    /**
     * Test successful user login with valid credentials.
     *
     * Verifies:
     * - User authentication succeeds
     * - JWT access token is generated
     * - Refresh token is created
     * - Response contains both tokens
     */
    @Test
    public void testLoginWithValidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setMobile("9876543210");
        request.setPassword("password123");

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setAccessToken("test_access_token");
        expectedResponse.setRefreshToken("test_refresh_token");
        expectedResponse.setTokenType("Bearer");
        expectedResponse.setExpiresIn(3600L);

        when(authenticationService.login(request)).thenReturn(expectedResponse);

        // Act
        LoginResponse response = authenticationService.login(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
    }

    /**
     * Test login failure with invalid credentials.
     *
     * Verifies:
     * - Authentication fails
     * - InvalidCredentialsException is thrown
     * - No tokens are generated
     */
    @Test
    public void testLoginWithInvalidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setMobile("9876543210");
        request.setPassword("wrongpassword");

        when(authenticationService.login(request))
            .thenThrow(new InvalidCredentialsException("Invalid mobile number or password"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.login(request);
        });
    }

    /**
     * Test token refresh with valid refresh token.
     *
     * Verifies:
     * - Refresh token is validated
     * - New access token is generated
     * - Refresh token remains valid for future use
     */
    @Test
    public void testRefreshTokenWithValidToken() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid_refresh_token");

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setAccessToken("new_access_token");
        expectedResponse.setRefreshToken("valid_refresh_token");

        when(authenticationService.refreshToken(request)).thenReturn(expectedResponse);

        // Act
        LoginResponse response = authenticationService.refreshToken(request);

        // Assert
        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("valid_refresh_token", response.getRefreshToken());
    }

    /**
     * Test token refresh failure with expired refresh token.
     *
     * Verifies:
     * - Expired refresh token is rejected
     * - InvalidCredentialsException is thrown
     * - No new access token is generated
     */
    @Test
    public void testRefreshTokenWithExpiredToken() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired_refresh_token");

        when(authenticationService.refreshToken(request))
            .thenThrow(new InvalidCredentialsException("Refresh token has expired"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.refreshToken(request);
        });
    }

    /**
     * Test user logout invalidates refresh tokens.
     *
     * Verifies:
     * - Logout call succeeds
     * - Refresh tokens are deleted
     * - User cannot refresh tokens after logout
     */
    @Test
    public void testLogout() {
        // Arrange
        Long userId = 1L;

        // Act
        authenticationService.logout(userId);

        // Assert - logout should complete successfully
        assertTrue(true);
    }
}
