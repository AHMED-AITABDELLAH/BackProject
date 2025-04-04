package com.savorybites.backend.repository;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);


    Optional<PasswordResetToken> findFirstByUserOrderByExpiryDateDesc(LocalUser user);
}