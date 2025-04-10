package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.UserDetailsBody;
import com.savorybites.backend.exception.PasswordIncorrectException;
import com.savorybites.backend.exception.UserAlreadyExistsException;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.service.EncryptionService;
import com.savorybites.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.savorybites.backend.model.LocalUser;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/accountDetails")
    public LocalUser getLoggedInUserDetails(@AuthenticationPrincipal LocalUser user){
        return user;
    }

    @Transactional
    @PostMapping("/updateAccountDetails")
    public ResponseEntity<Object> updateDetails(@AuthenticationPrincipal LocalUser user,@RequestBody UserDetailsBody userDetailsBody){
        return userService.update(user,userDetailsBody);
    }

    @GetMapping("/address")
    public void get(){

    }

    @GetMapping("/paymentMethods")
    public void get2(){

    }

}
