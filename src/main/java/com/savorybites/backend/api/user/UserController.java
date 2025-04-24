package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.user.UserDetailsBody;
import com.savorybites.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.savorybites.backend.model.LocalUser;


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

}
