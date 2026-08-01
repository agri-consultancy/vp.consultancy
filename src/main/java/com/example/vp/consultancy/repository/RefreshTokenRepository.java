package com.example.vp.consultancy.repository;

import com.example.vp.consultancy.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUser_Id(Long userId);
    void deleteByToken(String token);
    void deleteByUser_Id(Long userId);
}
