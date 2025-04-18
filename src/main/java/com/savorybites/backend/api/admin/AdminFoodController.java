package com.savorybites.backend.api.admin;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.api.dto.food.FoodDTO;
import com.savorybites.backend.model.*;
import com.savorybites.backend.service.AdminService;
import com.savorybites.backend.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/foods")
public class AdminFoodController {

    private final AdminService adminService;

    public AdminFoodController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping("/")
    public List<FoodDTO> getAllFoods(@AuthenticationPrincipal Admin admin,
                                     @RequestParam(required = false) FoodStatus foodStatus){
        List<Food> foodList;
        if(foodStatus==null)
            foodList = adminService.getAllFood(admin);
        else
            foodList = adminService.getAllFoodByStatus(admin,foodStatus);
        return foodList.stream().map(FoodDTO::new).toList();
    }

    @GetMapping("/{foodID}")
    public FoodDTO getFood(@AuthenticationPrincipal Admin admin,
                           @PathVariable long foodID){
        return new FoodDTO(adminService.getFood(admin,foodID));
    }

    @PutMapping("/{foodID}/toggleFlag")
    public ResponseEntity<String> toggleFlagOnFood(@AuthenticationPrincipal Admin admin,
                                                   @PathVariable long foodID){
        return adminService.toggleFlagOnFood(admin,foodID);
    }


    @GetMapping("/foodStatus")
    public FoodStatus[] getAllFoodStatuses() {
        return FoodStatus.values();
    }

}
