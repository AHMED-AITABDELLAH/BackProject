package com.savorybites.backend.api.admin;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import com.savorybites.backend.service.EmailService;
import com.savorybites.backend.service.RestaurantService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    public final RestaurantRepository restaurantRepository;
    public final RestaurantService restaurantService;

    public AdminController(EmailService emailService, VerificationTokenRepository verificationTokenRepository, RestaurantRepository restaurantRepository, RestaurantService restaurantService) {
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }

    @PostMapping("/approveRestaurants/{id}")
    @Transactional
    public ResponseEntity<?> approveRestaurant(@PathVariable Long id) {

        Restaurant restaurant = restaurantRepository
                .findRestaurantById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (restaurant.getIsApproved())
            return ResponseEntity.badRequest().body("Restaurant already approved");

        restaurant.setIsApproved(true);
        restaurantRepository.save(restaurant);

        // Now generate verification token and send email
        VerificationToken token = restaurantService.createVerificationToken(restaurant);
        verificationTokenRepository.save(token);
        emailService.sendRestaurantVerificationEmail(token);

        return ResponseEntity.ok("Restaurant approved and verification email sent.");
    }
}