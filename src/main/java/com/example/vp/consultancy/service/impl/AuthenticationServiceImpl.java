package com.example.vp.consultancy.service.impl;

import com.example.vp.consultancy.config.JwtTokenUtil;
import com.example.vp.consultancy.dto.LoginRequest;
import com.example.vp.consultancy.dto.LoginResponse;
import com.example.vp.consultancy.dto.RefreshTokenRequest;
import com.example.vp.consultancy.dto.UserResponse;
import com.example.vp.consultancy.entity.RefreshToken;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.exception.InvalidCredentialsException;
import com.example.vp.consultancy.exception.ResourceNotFoundException;
import com.example.vp.consultancy.repository.UserRepository;
import com.example.vp.consultancy.service.AuthenticationService;
import com.example.vp.consultancy.service.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of AuthenticationService.
 * Handles all authentication-related operations.
 */
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                   JwtTokenUtil jwtTokenUtil,
                                   RefreshTokenService refreshTokenService,
                                   UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
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
            
            UserResponse userResp = new UserResponse();
            userResp.setId(user.getId());
            userResp.setMobile(user.getMobile());
            userResp.setRole(user.getRole().toString());
            response.setUser(userResp);
            
            return response;

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid mobile number or password");
        }
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        Assert.notNull(request, "Refresh token request cannot be null");
        Assert.hasText(request.getRefreshToken(), "Refresh token cannot be empty");

        String token = request.getRefreshToken();
        
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        
        if (refreshTokenService.isExpired(refreshToken)) {
            throw new InvalidCredentialsException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (user == null) {
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
        
        UserResponse userResp = new UserResponse();
        userResp.setId(user.getId());
        userResp.setMobile(user.getMobile());
        userResp.setRole(user.getRole().toString());
        response.setUser(userResp);
        
        return response;
    }

    @Override
    public void logout(Long userId) {
        Assert.notNull(userId, "User ID cannot be null");
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        refreshTokenService.deleteByUserId(userId);
    }
}
