package com.savorybites.backend.api.restaurant;

import com.savorybites.backend.api.dto.food.FoodDetailsBody;
import com.savorybites.backend.api.dto.food.FoodRegistrationBody;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurant/foodItem")
public class FoodController {


    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/")
    public List<FoodDetailsBody> getRestaurantFood(@AuthenticationPrincipal Restaurant restaurant)
    {
    return foodService.getRestaurantFood(restaurant);
    }

    @PostMapping("/{foodID}/update")
    public ResponseEntity<Object> updateFoodItem(@AuthenticationPrincipal Restaurant restaurant,
                                                 @PathVariable long foodID,
                                                 @RequestBody FoodRegistrationBody foodRegistrationBody)
    {
        return foodService.updateFood(restaurant,foodRegistrationBody, foodID);
    }

    @GetMapping("/{foodID}")
    public FoodDetailsBody getFoodDetails (@AuthenticationPrincipal Restaurant restaurant,
                                           @PathVariable long foodID)
    {
        return foodService.getFood(foodID,restaurant);
    }




    @PostMapping("/{foodID}/toggle")
    public ResponseEntity<String> disableFoodItem (@AuthenticationPrincipal Restaurant restaurant,
                                                   @PathVariable long foodID)
    {
        boolean success = foodService.toggleFood(foodID, restaurant);
        return success
                ? ResponseEntity.ok("Food item is enabled successfully.")
                : ResponseEntity.ok("Food item is disabled successfully.");
    }




    @PostMapping("/{foodID}/delete")
    public ResponseEntity<String> deleteFoodItem (@AuthenticationPrincipal Restaurant restaurant,
                                                  @PathVariable long foodID)
    {

        boolean success = foodService.deleteFood(foodID, restaurant);
        return success
                ? ResponseEntity.ok("The food item is deleted successfully.")
                : ResponseEntity.badRequest().body("Something went wrong.");
    }

    @PostMapping("/addItem")
    public ResponseEntity<Object> addFoodItem (@AuthenticationPrincipal Restaurant
                                                       restaurant, @RequestBody FoodRegistrationBody foodRegistrationBody)
    {
        foodService.addFood(restaurant, foodRegistrationBody);
        return ResponseEntity.ok("Your Food Item is added successfully");
    }
}
