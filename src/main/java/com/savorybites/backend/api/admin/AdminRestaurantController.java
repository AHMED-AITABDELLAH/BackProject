package com.savorybites.backend.api.admin;

import com.savorybites.backend.api.dto.restaurant.RestaurantDTO;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.Status;
import com.savorybites.backend.service.AdminService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {

    private final AdminService adminService;

    public AdminRestaurantController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/{restaurantID}/approve")
    @Transactional
    public ResponseEntity<?> approveRestaurant(@AuthenticationPrincipal Admin admin,
                                               @PathVariable Long restaurantID) {
        return adminService.approveRestaurant(admin,restaurantID);
    }

    @PutMapping("/{restaurantID}/decline")
    @Transactional
    public ResponseEntity<?> declineRestaurant(@AuthenticationPrincipal Admin admin,
                                               @PathVariable Long restaurantID) {
        return adminService.declineRestaurant(admin,restaurantID);
    }

    @PutMapping("/{restaurantID}/ban")
    @Transactional
    public ResponseEntity<?> banRestaurant(@AuthenticationPrincipal Admin admin,
                                           @PathVariable Long restaurantID) {
        return adminService.banRestaurant(admin,restaurantID);
    }

    @DeleteMapping("/{restaurantID}")
    @Transactional
    public ResponseEntity<?> deleteRestaurant(@AuthenticationPrincipal Admin admin,
                                              @PathVariable Long restaurantID) {
        return adminService.deleteRestaurant(admin,restaurantID);
    }

    @GetMapping("/{restaurantID}")
    public RestaurantDTO getRestaurant(@AuthenticationPrincipal Admin admin,
                                             @PathVariable Long restaurantID) {
        return new RestaurantDTO(adminService.getRestaurant(admin,restaurantID));
    }


    @GetMapping("/")
    public List<RestaurantDTO> getRestaurants(@AuthenticationPrincipal Admin admin,
                                              @RequestParam(required = false) Status status){
        List<Restaurant> restaurantList;

        if(status == null)
            restaurantList= adminService.getAllRestaurants(admin);
        else
            restaurantList = adminService.getRestaurantByStatus(admin,status);

        return restaurantList.stream().map(RestaurantDTO::new).toList();
    }
}
