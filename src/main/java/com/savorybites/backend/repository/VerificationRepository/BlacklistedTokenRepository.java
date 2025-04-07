package com.savorybites.backend.repository.VerificationRepository;

import com.savorybites.backend.model.verificationModels.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByToken(String token);

    void deleteByExpiryDateBefore(Instant expiryDate);
}