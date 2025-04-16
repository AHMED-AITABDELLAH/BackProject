package com.savorybites.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    private static final int MAX_PASSWORD_LENGTH = 72;

    @Value("${encryption.salt.rounds}")
    private int saltRounds;
    public String salt;

    @PostConstruct
    public void postConstruct(){
        salt= BCrypt.gensalt(saltRounds);
    }

    public String encryptPassword(String password) {
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password is too long. Maximum allowed length is " + MAX_PASSWORD_LENGTH);
        }
        return BCrypt.hashpw(password, salt);
    }

    public Boolean verifyPassword(String password,String hash){
        return BCrypt.checkpw(password,hash);
    }
}
