package com.savorybites.backend.api.auth;

import com.savorybites.backend.exception.user.EmailAlreadySentException;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.service.BlacklistedTokenService;
import com.savorybites.backend.service.PasswordResetTokenService;
import com.savorybites.backend.service.RestaurantService;
import com.savorybites.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController
{

    private final PasswordResetTokenService passwordResetTokenService;
    private final UserService userService;
    private final BlacklistedTokenService blacklistedTokenService;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;


    public AuthController(PasswordResetTokenService passwordResetTokenService, UserService userService, BlacklistedTokenService blacklistedTokenService, UserRepository userRepository, RestaurantRepository restaurantRepository, RestaurantService restaurantService) {
        this.passwordResetTokenService = passwordResetTokenService;
        this.userService = userService;
        this.blacklistedTokenService = blacklistedTokenService;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) throws EmailAlreadySentException {
        if(userRepository.findByEmailIgnoreCase(email).isPresent()){
            userService.forgotPassword(email);
        }
        else if(restaurantRepository.findByEmailIgnoreCase(email).isPresent()){
            restaurantService.forgotPassword(email);
        }
        return ResponseEntity.ok("Password reset link sent to your email.");
    }


    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {

        boolean success = passwordResetTokenService.resetPassword(token, newPassword);
        return success
                ? ResponseEntity.ok("Password successfully reset.")
                : ResponseEntity.badRequest().body("Invalid or expired token.");

    }

    @PostMapping("/verifyAccount")
    public ResponseEntity<Object> verifyAccount(@RequestParam String token) {
        if (userService.verifyAccount(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // This triggers the LogoutHandler and LogoutSuccessHandler (i.e., your LogoutService)
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
}
