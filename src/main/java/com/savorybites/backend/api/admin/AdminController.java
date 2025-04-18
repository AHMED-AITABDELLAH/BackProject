package com.savorybites.backend.api.admin;
import com.savorybites.backend.api.dto.admin.DashboardStatistics;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.service.AdminService;
import com.savorybites.backend.service.RestaurantService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    public final RestaurantRepository restaurantRepository;
    public final RestaurantService restaurantService;
    private final AdminService adminService;

    public AdminController(RestaurantRepository restaurantRepository,
                           RestaurantService restaurantService,
                           AdminService adminService) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.adminService = adminService;
    }

    @PostMapping("/approveRestaurants/{restaurantID}")
    @Transactional
    public ResponseEntity<?> approveRestaurant(@AuthenticationPrincipal Admin admin,
                                               @PathVariable Long restaurantID) {
        return adminService.approveRestaurant(admin,restaurantID);
    }

    @GetMapping("/dashboard")
    public DashboardStatistics approveRestaurant(@AuthenticationPrincipal Admin admin) {
        return adminService.getDashboardStats(admin);
    }


}