package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.UserDetailsBody;
import com.savorybites.backend.exception.UserAlreadyExistsException;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.service.EncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.savorybites.backend.model.LocalUser;

@RestController
@RequestMapping("/user")
public class UserController {


    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    public UserController(EncryptionService encryptionService, UserRepository userRepository) {
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/accountDetails")
    public LocalUser getLoggedInUserDetails(@AuthenticationPrincipal LocalUser user){
        return user;
    }

    @PostMapping("/updateAccountDetails")
    public ResponseEntity<Object> updateDetails(@AuthenticationPrincipal LocalUser user,@RequestBody UserDetailsBody userDetailsBody){

        user.setProfileImg(userDetailsBody.getProfileImg());
        user.setLastName(userDetailsBody.getLastName());
        user.setFirstName(userDetailsBody.getFirstName());
        user.setPhone(userDetailsBody.getPhone());

        if(userRepository.findByEmailIgnoreCase(user.getEmail()).isPresent())
            throw new UserAlreadyExistsException();

        user.setEmail(userDetailsBody.getEmail());
        user.setPassword(encryptionService.encryptPassword(userDetailsBody.getPassword()));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/address")
    public void get(){

    }

    @GetMapping("/paymentMethods")
    public void get2(){

    }

}
