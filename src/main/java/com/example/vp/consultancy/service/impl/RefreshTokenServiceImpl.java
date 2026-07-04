package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.entity.RefreshToken;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.repository.RefreshTokenRepository;
import com.example.vp.consultancy.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of RefreshTokenService.
 * 
 * This service manages the lifecycle of refresh tokens including:
 * - Token creation with unique values and expiration times
 * - Token validation and retrieval
 * - Expiration checking
 * - Token cleanup on logout
 * 
 * Refresh tokens have a longer expiration time than access tokens
 * (typically 7 days vs 1 hour) and are used to obtain new access tokens
 * without requiring the user to re-authenticate.
 */
@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refresh-ms}")
    private Long refreshDurationMs;

    // Refresh token expiration time: 7 days in milliseconds
    private static final long REFRESH_TOKEN_EXPIRATION = 7L * 24 * 60 * 60 * 1000;
    
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Constructor with dependency injection.
     * 
     * @param refreshTokenRepository repository for refresh token database operations
     */
    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * {@inheritDoc}
     * 
     * Creates a new refresh token for a user.
     * 
     * Flow:
     * 1. Validate user is not null
     * 2. Generate unique token using UUID
     * 3. Set expiration time to 7 days from now
     * 4. Save token to database
     * 5. Return the created token entity
     */
    @Override
    public RefreshToken createRefreshToken(User user) {
        Assert.notNull(user, "User cannot be null");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        
        // Generate unique token using UUID
        refreshToken.setToken(UUID.randomUUID().toString());
        
        // Set expiration time to 7 days from now
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshDurationMs));

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * {@inheritDoc}
     * 
     * Finds a refresh token by its token value.
     * 
     * Flow:
     * 1. Validate token string is not null or empty
     * 2. Query database for token
     * 3. Return Optional with token if found, empty if not
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        Assert.hasText(token, "Token cannot be null or empty");
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * {@inheritDoc}
     * 
     * Checks if a refresh token has expired.
     * 
     * Flow:
     * 1. Validate token is not null
     * 2. Compare token expiration time with current time
     * 3. Return true if token has expired, false otherwise
     */
    @Transactional(readOnly = true)
    @Override
    public boolean isExpired(RefreshToken token) {
        Assert.notNull(token, "Refresh token cannot be null");
        return Instant.now().isAfter(token.getExpiryDate());
    }

    /**
     * {@inheritDoc}
     * 
     * Deletes all refresh tokens associated with a user.
     * 
     * Flow:
     * 1. Validate userId is not null and positive
     * 2. Delete all tokens for this user from database
     * 3. This invalidates all active sessions for the user
     */
    @Override
    public void deleteByUserId(Long userId) {
        Assert.notNull(userId, "User ID cannot be null");
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        refreshTokenRepository.deleteByUserId(userId);
    }
}
