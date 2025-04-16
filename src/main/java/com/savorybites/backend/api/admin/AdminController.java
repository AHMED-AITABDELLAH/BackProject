package com.savorybites.backend.api.admin;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import com.savorybites.backend.service.AdminService;
import com.savorybites.backend.service.EmailService;
import com.savorybites.backend.service.RestaurantService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    public final RestaurantRepository restaurantRepository;
    public final RestaurantService restaurantService;
    private final AdminService adminService;

    public AdminController(EmailService emailService, VerificationTokenRepository verificationTokenRepository, RestaurantRepository restaurantRepository, RestaurantService restaurantService, AdminService adminService) {
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.adminService = adminService;
    }

    @PostMapping("/approveRestaurants/{id}")
    @Transactional
    public ResponseEntity<?> approveRestaurant(@AuthenticationPrincipal Admin admin,@PathVariable Long restaurantID) {
        return adminService.approveRestaurant(admin,restaurantID);
    }
}