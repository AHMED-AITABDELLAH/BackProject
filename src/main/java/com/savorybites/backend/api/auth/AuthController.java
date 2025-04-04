package com.savorybites.backend.api.auth;

import com.savorybites.backend.api.model.LoginBody;
import com.savorybites.backend.api.model.LoginResponse;
import com.savorybites.backend.api.model.RegistrationBody;
import com.savorybites.backend.exception.EmailAlreadySentException;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.service.EmailService;
import com.savorybites.backend.service.PasswordResetTokenService;
import com.savorybites.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;

    public AuthController(UserService userService, PasswordResetTokenService passwordResetTokenService) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<LocalUser> registerUser
            (@Valid @RequestBody RegistrationBody registrationBody)
    {
        userService.Registration(registrationBody);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser
            (@Valid @RequestBody LoginBody loginBody) {

        String jwtToken = userService.Login(loginBody);
        LoginResponse response = new LoginResponse();
        response.setJwt(jwtToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifyEmail")
    public ResponseEntity verifyEmail(@RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @GetMapping("/me")
    public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user){
        return user;
    }


    @PostMapping("/forgotPassword")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) throws EmailAlreadySentException {
        userService.forgotPassword(email);
        return ResponseEntity.ok("Password reset link sent to your email.");
    }


    @GetMapping("/verifyPassword")
    public ResponseEntity<String> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetTokenService.isResetTokenValid(token);
        return isValid ? ResponseEntity.ok("Valid token.") : ResponseEntity.badRequest().body("Invalid or expired token.");
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        boolean success = passwordResetTokenService.resetPassword(token, newPassword);
        return success ? ResponseEntity.ok("Password successfully reset.") : ResponseEntity.badRequest().body("Invalid or expired token.");
    }












}
