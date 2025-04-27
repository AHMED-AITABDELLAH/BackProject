package com.savorybites.backend.api.restaurant;
import com.savorybites.backend.api.dto.restaurant.RestaurantDTO;
import com.savorybites.backend.api.dto.restaurant.RestaurantDashboardStatistics;
import com.savorybites.backend.api.dto.restaurant.RestaurantDetailsBody;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.DashboardService;
import com.savorybites.backend.service.RestaurantOnboardingService;
import com.savorybites.backend.service.RestaurantService;
import com.stripe.exception.StripeException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/restaurant")
public class RestaurantController {


    private final RestaurantService restaurantService;
    private final DashboardService dashboardService;
    private final RestaurantOnboardingService restaurantOnboardingService;

    @GetMapping("/isSetupComplete")
    public ResponseEntity<Boolean> isSetupComplete(@AuthenticationPrincipal Restaurant restaurant){
        return ResponseEntity.ok(restaurant.getIsSetupComplete());
    }

    @PostMapping("/stripe/results")
    public ResponseEntity<?> ifResults(@AuthenticationPrincipal Restaurant restaurant,
                                       @RequestParam Boolean result){
        if(result){
            restaurantService.setSetupCompleted(restaurant);
            return ResponseEntity.ok("Your account setup is completed successfully");
        }else
            return ResponseEntity.badRequest().body("Something went wrong please try setting up the connect Strip Account to complete your setup.");

    }

    @GetMapping("/getOnboardingLink")
    public ResponseEntity<String> getOnboardingLink(@AuthenticationPrincipal Restaurant restaurant) throws StripeException {
        return restaurantOnboardingService.onboardRestaurant(restaurant);
    }

    @GetMapping("/accountDetails")
    public RestaurantDTO getLoggedInRestaurantDetails (@AuthenticationPrincipal Restaurant restaurant)
    {
        return new RestaurantDTO(restaurant);
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

    @GetMapping("/dashboard")
    public RestaurantDashboardStatistics getDashboard(@AuthenticationPrincipal Restaurant restaurant){
        return dashboardService.getDashboardStats(restaurant);
    }
}