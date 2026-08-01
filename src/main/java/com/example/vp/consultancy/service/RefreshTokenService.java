package com.example.vp.consultancy.service;

import com.example.vp.consultancy.entity.RefreshToken;
import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    @Transactional(readOnly = true)
    Optional<RefreshToken> findByToken(String token);

    @Transactional(readOnly = true)
    List<RefreshToken> findByUserId(Long userId);

    @Transactional(readOnly = true)
    boolean isExpired(RefreshToken token);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);

//
//    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
//        this.refreshTokenRepository = refreshTokenRepository;
//    }
//
//    public RefreshToken createRefreshToken(User user) {
//        RefreshToken token = RefreshToken.builder()
//                .token(UUID.randomUUID().toString())
//                .user(user)
//                .expiryDate(Instant.now().plusMillis(refreshDurationMs))
//                .build();
//        return refreshTokenRepository.save(token);
//    }
//
//    public Optional<RefreshToken> findByToken(String token) {
//        return refreshTokenRepository.findByToken(token);
//    }
//
//    public boolean isExpired(RefreshToken token) {
//        return token.getExpiryDate().isBefore(Instant.now());
//    }
//
//    public void deleteByUserId(Long userId) {
//        refreshTokenRepository.deleteByUserId(userId);
//    }
}
