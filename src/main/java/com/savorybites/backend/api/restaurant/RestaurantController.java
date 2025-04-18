package com.savorybites.backend.api.restaurant;
import com.savorybites.backend.api.dto.restaurant.RestaurantDetailsBody;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.RestaurantService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurant")
public class RestaurantController {


    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService)
    {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/accountDetails")
    public Restaurant getLoggedInRestaurantDetails (@AuthenticationPrincipal Restaurant restaurant)
    {
        return restaurant;
    }

    @Transactional
    @PostMapping("/updateAccountDetails")
    public ResponseEntity<Object> updateDetails (
            @AuthenticationPrincipal Restaurant restaurant,
            @RequestBody RestaurantDetailsBody restaurantDetailsBody
    )
    {
        return restaurantService.update(restaurant, restaurantDetailsBody);
    }
}