package com.savorybites.backend.api.auth;
import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.auth.LoginResponse;
import com.savorybites.backend.api.dto.auth.RestaurantRegistrationBody;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.service.EmailService;
import com.savorybites.backend.service.RestaurantService;
import com.savorybites.backend.service.StripeService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/auth/restaurant")
public class AuthRestaurantController {


    private final RestaurantService restaurantService;
    private final StripeService stripeService;
    private final EmailService emailService;
    private final RestaurantRepository restaurantRepository;


    public AuthRestaurantController(RestaurantService restaurantService,
                                    StripeService stripeService,
                                    EmailService emailService,
                                    RestaurantRepository restaurantRepository) {
        this.restaurantService = restaurantService;
        this.stripeService = stripeService;
        this.emailService = emailService;
        this.restaurantRepository = restaurantRepository;
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser
            (@Valid @RequestBody RestaurantRegistrationBody restaurantRegistrationBody)
    {
        return restaurantService.registerRestaurant(restaurantRegistrationBody);
    }

    @PostMapping("/register/{restaurantId}/payment")
    public ResponseEntity<?> handleStripeOnboarding(@PathVariable Long restaurantId) throws StripeException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (restaurant.getStripeAccountId() != null) {
            return ResponseEntity.badRequest().body("Stripe account already exists");
        }

        String onboardingUrl = stripeService.createExpressAccountAndOnboardingLink(restaurant);
        emailService.sendAccountIsWaitingApproval(restaurant);
        return ResponseEntity.ok(Map.of("onboardingUrl", onboardingUrl));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser
            (@Valid @RequestBody LoginBody loginBody) {

        String jwtToken = restaurantService.loginRestaurant(loginBody);
        LoginResponse response = new LoginResponse(jwtToken);
        return ResponseEntity.ok(response);
    }







}
