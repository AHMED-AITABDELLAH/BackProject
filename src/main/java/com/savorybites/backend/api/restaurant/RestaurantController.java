package com.savorybites.backend.api.restaurant;

import com.savorybites.backend.api.dto.RestaurantDetailsBody;
import com.savorybites.backend.api.dto.UserDetailsBody;
import com.savorybites.backend.dto.LocalUser;
import com.savorybites.backend.api.dto.FoodRegistrationBody;
import com.savorybites.backend.exception.UserNotFoundException;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.RestaurantService;
import jakarta.transaction.Transactional;
import com.savorybites.backend.repository.FoodRepository;
import com.savorybites.backend.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/restaurant")
public class RestaurantController {


    private final RestaurantService restaurantService;
    private final FoodService foodService;
    private final FoodRepository foodRepository;

    public RestaurantController(RestaurantService restaurantService,FoodService foodService, FoodRepository foodRepository) {
        this.foodService = foodService;
        this.foodRepository = foodRepository;
        this.restaurantService=restaurantService;
    }


    @PostMapping("/updateFood/{foodID}")
    public ResponseEntity<Object> updateFoodItem(@AuthenticationPrincipal Restaurant restaurant,
                                                 @PathVariable long foodID,
                                                 @RequestBody FoodRegistrationBody foodRegistrationBody){

        return foodService.updateFood(restaurant,foodRegistrationBody,foodID);
    }


    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    @GetMapping("/foodItem/{foodID}")
    public Food getFoodDetails(@AuthenticationPrincipal Restaurant restaurant,
                                         @PathVariable long foodID){
        Optional<Food> opFood = foodRepository.findFoodByIdAndRestaurant(foodID,restaurant);
        if(opFood.isPresent())
            return opFood.get();
        else
            //TODO : Add Food not found exception
            throw new UserNotFoundException();
    }

    //TODO ADD the  1 address to it
    @GetMapping("/accountDetails")
    public Restaurant getLoggedInRestaurantDetails(@AuthenticationPrincipal Restaurant restaurant){
        return restaurant;


    @PostMapping("/foodItem/{foodID}/disable")
    public ResponseEntity<String> disableFoodItem(@AuthenticationPrincipal Restaurant restaurant,
                                                  @PathVariable long foodID){
        boolean success = foodService.disableFood(foodID,restaurant);
        return success
                ? ResponseEntity.ok("Food item is disabled successfully.")
                : ResponseEntity.badRequest().body("Something went wrong.");
    }


    //TODO ADD the  1 address to it
    @Transactional
    @PostMapping("/updateAccountDetails")
    public ResponseEntity<Object> updateDetails(
            @AuthenticationPrincipal Restaurant restaurant,
             @RequestBody RestaurantDetailsBody restaurantDetailsBody
    )
    {
        return restaurantService.update(restaurant,restaurantDetailsBody);
    @PostMapping("/foodItem/{foodID}/delete")
    public ResponseEntity<String> deleteFoodItem(@AuthenticationPrincipal Restaurant restaurant,
                                                 @PathVariable long foodID){

        boolean success= foodService.deleteFood(foodID,restaurant);
        return success
                ? ResponseEntity.ok("Password successfully reset.")
                : ResponseEntity.badRequest().body("Invalid or expired token.");
    }

    @PostMapping("/foodItem/addItem")
    public ResponseEntity<Object> addFoodItem(@AuthenticationPrincipal Restaurant restaurant,@RequestBody FoodRegistrationBody foodRegistrationBody){
        foodService.addFood(restaurant,foodRegistrationBody);
        return ResponseEntity.ok("Your Food Item is added successfully");
    }




}
