package com.savorybites.backend.api.auth;
import com.savorybites.backend.api.model.LoginBody;
import com.savorybites.backend.api.model.LoginResponse;
import com.savorybites.backend.api.model.RestaurantRegistrationBody;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/restaurant")
public class AuthRestaurantController {


    private final RestaurantService restaurantService;


    public AuthRestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }


    @PostMapping("/register")
    public ResponseEntity<Restaurant> registerUser
            (@Valid @RequestBody RestaurantRegistrationBody restaurantRegistrationBody)
    {
        restaurantService.registerRestaurant(restaurantRegistrationBody);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser
            (@Valid @RequestBody LoginBody loginBody) {

        String jwtToken = restaurantService.loginRestaurant(loginBody);
        LoginResponse response = new LoginResponse();
        response.setJwt(jwtToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public Restaurant getLoggedInUserProfile(@AuthenticationPrincipal Restaurant restaurant){
        System.out.println(restaurant.getEmail());

        return restaurant;
    }






}
