package com.savorybites.backend.repository;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface VerificationTokenRepository extends ListCrudRepository<VerificationToken,Long> {



    void deleteByUser(LocalUser User);

    Optional<VerificationToken> findByToken(String token);
}
