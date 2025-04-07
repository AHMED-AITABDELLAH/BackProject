package com.savorybites.backend.repository.VerificationRepository;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public interface VerificationTokenRepository extends ListCrudRepository<VerificationToken,Long> {



    void deleteByUser(LocalUser User);

    Optional<VerificationToken> findByToken(String token);

    void deleteByExpiryDateBefore(Instant instant);

    void deleteByRestaurant(Restaurant restaurant);
}
