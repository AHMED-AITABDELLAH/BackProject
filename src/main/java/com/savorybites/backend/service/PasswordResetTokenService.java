package com.savorybites.backend.service;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.PasswordResetToken;
import com.savorybites.backend.repository.PasswordResetTokenRepository;
import com.savorybites.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;


    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, EncryptionService encryptionService, UserRepository userRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
    }

    public boolean isResetTokenValid(String token){
        Optional<PasswordResetToken> opToken=passwordResetTokenRepository.findByToken(token);
        if(opToken.isPresent()){
          PasswordResetToken passwordResetToken=opToken.get();
          return passwordResetToken.getExpiryDate().isAfter(Instant.now());
        }
        return false;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        if (isResetTokenValid(token)) {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).get();
            // Reset the user's password
            LocalUser user = resetToken.getUser();
            user.setPassword(encryptionService.encryptPassword(newPassword));
            userRepository.save(user);

            // Delete the token after successful reset
            passwordResetTokenRepository.delete(resetToken);
            return true;
        }
        return false;
    }

}