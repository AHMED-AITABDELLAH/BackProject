package com.savorybites.backend.api.auth;
import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.auth.LoginResponse;
import com.savorybites.backend.api.dto.auth.RestaurantRegistrationBody;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.service.EmailService;
import com.savorybites.backend.service.RestaurantOnboardingService;
import com.savorybites.backend.service.RestaurantService;
import com.savorybites.backend.service.StripeService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/restaurant")
public class AuthRestaurantController {


    private final RestaurantService restaurantService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser
            (@Valid @RequestBody RestaurantRegistrationBody restaurantRegistrationBody) throws StripeException {

        return restaurantService.registerRestaurant(restaurantRegistrationBody);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser
            (@Valid @RequestBody LoginBody loginBody) {

        String jwtToken = restaurantService.loginRestaurant(loginBody);
        LoginResponse response = new LoginResponse(jwtToken);
        return ResponseEntity.ok(response);
    }







}
