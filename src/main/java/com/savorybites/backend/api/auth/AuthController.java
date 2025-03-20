package com.savorybites.backend.api.auth;

import com.savorybites.backend.api.model.LoginBody;
import com.savorybites.backend.api.model.LoginResponse;
import com.savorybites.backend.api.model.RegistrationBody;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.service.LocalUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LocalUserService localUserService;

    public AuthController(LocalUserService localUserService) {
        this.localUserService = localUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<LocalUser> registerUser
            (@Valid @RequestBody RegistrationBody registrationBody)
    {
        localUserService.Registration(registrationBody);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser
            (@Valid @RequestBody LoginBody loginBody) {

        String jwtToken = localUserService.Login(loginBody);
        LoginResponse response = new LoginResponse();
        response.setJwt(jwtToken);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user){
        return user;
    }
}
