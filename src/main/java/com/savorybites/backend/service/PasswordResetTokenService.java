package com.savorybites.backend.service;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.verificationModels.PasswordResetToken;
import com.savorybites.backend.repository.VerificationRepository.PasswordResetTokenRepository;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final RestaurantRepository restaurantRepository;


    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, EncryptionService encryptionService, UserRepository userRepository, JWTService jwtService, RestaurantRepository restaurantRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.restaurantRepository = restaurantRepository;
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
            String role=jwtService.getRoleFromToken(token);
            if("ROLE_USER".equals(role)){
                // Reset the user's password
                LocalUser user = resetToken.getUser();
                user.setPassword(encryptionService.encryptPassword(newPassword));
                userRepository.save(user);
            } else if ("ROLE_RESTAURANT".equals(role)) {
                // Reset the restaurant's password

                Restaurant restaurant = resetToken.getRestaurant();
                restaurant.setPassword(encryptionService.encryptPassword(newPassword));
                restaurantRepository.save(restaurant);
            }


            // Delete the token after successful reset
            passwordResetTokenRepository.delete(resetToken);
            return true;
        }
        return false;
    }

}