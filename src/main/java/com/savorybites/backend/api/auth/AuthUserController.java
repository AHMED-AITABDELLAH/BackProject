package com.savorybites.backend.api.auth;
import com.savorybites.backend.api.dto.LoginBody;
import com.savorybites.backend.api.dto.LoginResponse;
import com.savorybites.backend.api.dto.UserRegistrationBody;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.service.PasswordResetTokenService;
import com.savorybites.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth/user")
public class AuthUserController {

    private final UserService userService;

    public AuthUserController(UserService userService, PasswordResetTokenService passwordResetTokenService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<LocalUser> registerUser
            (@Valid @RequestBody UserRegistrationBody userRegistrationBody)
    {
        userService.Registration(userRegistrationBody);
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


    @GetMapping("/me")
    public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user){
        return user;
    }















}
