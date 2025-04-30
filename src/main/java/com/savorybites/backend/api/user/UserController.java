package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.user.UserDTO;
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
    public UserDTO getLoggedInUserDetails(@AuthenticationPrincipal LocalUser user){
        return new UserDTO(user);
    }

    @GetMapping("/accountDetails/hasPersonalDetails")
    public ResponseEntity<Boolean> hasPersonalDetails(@AuthenticationPrincipal LocalUser user){
        return userService.hasPersonalDetails(user);
    }

    @Transactional
    @PostMapping("/updateAccountDetails")
    public ResponseEntity<Object> updateDetails(@AuthenticationPrincipal LocalUser user,@RequestBody UserDetailsBody userDetailsBody){
        return userService.update(user,userDetailsBody);
    }

}
