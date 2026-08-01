package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.config.JwtAuthenticationFilter;
import com.example.vp.consultancy.config.JwtTokenUtil;
import com.example.vp.consultancy.dto.*;
import com.example.vp.consultancy.entity.RefreshToken;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.entity.UserProfile;
import com.example.vp.consultancy.exception.InvalidCredentialsException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.exception.UserAlreadyLoggedInException;
import com.example.vp.consultancy.repository.UserProfileRepository;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.service.AuthenticationService;
import com.example.vp.consultancy.service.RefreshTokenService;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of AuthenticationService.
 * Handles all authentication-related operations.
 */
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     JwtTokenUtil jwtTokenUtil,
                                     RefreshTokenService refreshTokenService,
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder, UserProfileRepository userProfileRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        logger.info("Attempting login for mobile: {}", request.getMobile());
        Assert.notNull(request, "Login request cannot be null");
        Assert.hasText(request.getMobile(), "Mobile number cannot be empty");
        Assert.hasText(request.getPassword(), "Password cannot be empty");

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getMobile(),
                    request.getPassword()
                )
            );
            User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            List<RefreshToken> existingTokens = refreshTokenService.findByUserId(user.getId());
            boolean hasActiveRefreshToken = false;
            for (RefreshToken existingToken : existingTokens) {
                if (refreshTokenService.isExpired(existingToken)) {
                    refreshTokenService.deleteByToken(existingToken.getToken());
                } else {
                    hasActiveRefreshToken = true;
                }
            }

            if (hasActiveRefreshToken) {
                logger.warn("User {} already has an active refresh token. Rejecting login for single-device enforcement.", user.getMobile());
                throw new UserAlreadyLoggedInException(
                        "User is already logged in on another device. Please logout from the previous device first.");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("mobile", user.getMobile());
            claims.put("role", user.getRole());
            String accessToken = jwtTokenUtil.generateToken(claims, user.getMobile());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken.getToken());
            response.setTokenType("Bearer");
            response.setExpiresIn(3600L);
            response.setRole(user.getRole().toString());
            response.setMobile(user.getMobile());
            userProfileRepository.findByUserId(user.getId()).ifPresent(
                userProfile -> {
                    UserDetailsDto userResp = new UserDetailsDto();
                    userResp.setId(user.getId());
                    userResp.setFirstName(userProfile.getFirstName());
                    userResp.setLastName(userProfile.getLastName());
                    userResp.setEmail(userProfile.getEmail());
                    response.setUserDetails(userResp);
                }
            );
            logger.info("Login successful for mobile: {} and response: {}", request.getMobile(), response);
            return response;

        } catch (AuthenticationException e) {
            logger.error("Authentication failed for mobile: {}", request.getMobile(), e);
            throw new InvalidCredentialsException("Invalid mobile number or password");
        }
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        Assert.notNull(request, "Refresh token request cannot be null");
        Assert.hasText(request.getRefreshToken(), "Refresh token cannot be empty");

        String token = request.getRefreshToken();
        logger.info("Attempting to refresh token: {}", token);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            logger.error("Refresh token not found: {}", token);
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        
        if (refreshTokenService.isExpired(refreshToken)) {
            logger.error("Refresh token has expired: {}", token);
            throw new InvalidCredentialsException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (user == null) {
            logger.error("User not found for refresh token: {}", token);
            throw new ResourceNotFoundException("User not found for refresh token");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("mobile", user.getMobile());
        claims.put("role", user.getRole());
        String accessToken = jwtTokenUtil.generateToken(claims, user.getMobile());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(3600L);
        response.setRole(user.getRole().toString());
        response.setMobile(user.getMobile());
        userProfileRepository.findByUserId(user.getId()).ifPresent(
                userProfile -> {
                    UserDetailsDto userDetailsDto = new UserDetailsDto();
                    userDetailsDto.setId(user.getId());
                    userDetailsDto.setFirstName(userProfile.getFirstName());
                    userDetailsDto.setLastName(userProfile.getLastName());
                    userDetailsDto.setEmail(userProfile.getEmail());
                    response.setUserDetails(userDetailsDto);
                }
        );
        logger.info("Token refreshed successfully for user: {} and response: {}", user.getMobile(), response);
        return response;
    }

    @Override
    public void logout(Long userId) {
        logger.info("Attempting logout for userId: {}", userId);
        Assert.notNull(userId, "User ID cannot be null");
        if (userId <= 0) {
            logger.error("Invalid user ID for logout: {}", userId);
            throw new IllegalArgumentException("User ID must be positive");
        }

        refreshTokenService.deleteByUserId(userId);
        logger.info("Logout successful for userId: {}", userId);
    }

    @Override
    public void changePassword(com.example.vp.consultancy.dto.UpdatePasswordRequest request) {
        Assert.notNull(request, "Update password request cannot be null");
        Assert.hasText(request.getOldPassword(), "Old password is required");
        Assert.hasText(request.getNewPassword(), "New password is required");

        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new com.example.vp.consultancy.exception.ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            logger.error("Old password does not match for user: {}", mobile);
            throw new com.example.vp.consultancy.exception.InvalidCredentialsException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", mobile);
    }
}
