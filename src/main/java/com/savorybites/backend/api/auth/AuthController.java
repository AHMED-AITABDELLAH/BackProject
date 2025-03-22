package com.savorybites.backend.api.auth;

import com.savorybites.backend.api.model.LoginBody;
import com.savorybites.backend.api.model.LoginResponse;
import com.savorybites.backend.api.model.RegistrationBody;
import com.savorybites.backend.exception.UserNotVerified;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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

    @PostMapping("/verify")
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












}
