package com.savorybites.backend.service;

import com.savorybites.backend.model.BlacklistedToken;
import com.savorybites.backend.repository.BlacklistedTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class BlacklistedTokenService {
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public BlacklistedTokenService(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }


    public void addBlacklistedToken(String token,int expiryMinutes){
        //Here We delete the expired token before creating new one
        blacklistedTokenRepository.deleteByExpiryDateBefore(Instant.now());
        BlacklistedToken blacklistedToken=new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiryDate(Instant.now().plusSeconds(expiryMinutes * 60));
        blacklistedTokenRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token){
        Optional<BlacklistedToken> blacklistedToken = blacklistedTokenRepository.findByToken(token);
        return
                blacklistedToken.isPresent()
                &&
                blacklistedToken.get().getExpiryDate().isAfter(Instant.now());
    }





}
