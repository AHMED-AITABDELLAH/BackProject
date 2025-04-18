package com.savorybites.backend.api.publics;

import com.savorybites.backend.api.dto.food.FoodDTO;
import com.savorybites.backend.api.dto.restaurant.RestaurantDTO;
import com.savorybites.backend.api.dto.restaurant.RestaurantShortDTO;
import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.CategoryService;
import com.savorybites.backend.service.FoodService;
import com.savorybites.backend.service.RestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
public class PublicController {


    private final FoodService foodService;
    private final CategoryService categoryService;
    private final RestaurantService restaurantService;

    public PublicController(FoodService foodService, CategoryService categoryService, RestaurantService restaurantService) {
        this.foodService = foodService;
        this.categoryService = categoryService;
        this.restaurantService = restaurantService;
    }

    @GetMapping("/onSaleFood")
    public List<FoodDTO> getOnSaleFood(){
        List<Food> foodList = foodService.OnSaleSection(5);
        return foodList.stream().map(FoodDTO::new).toList();
    }

    @GetMapping("/popularFood")
    public List<FoodDTO> getPopularFood(){
        List<Food> foodList = foodService.PopularFoodItems();
        return foodList.stream().map(FoodDTO::new).toList();
    }

    @GetMapping("/categories")
    public List<Category> getCategories(){
        List<Category> categoryList = categoryService.getCategories();
        Collections.shuffle(categoryList);
        return categoryList
                .stream()
                .limit(4)
                .collect(Collectors.toList());
    }

    @GetMapping("/popularRestaurant")
    public List<RestaurantShortDTO> getPopularRestaurant(){
        List<Restaurant> restaurantList = restaurantService.getPopularRestaurants();
        return restaurantList.stream().map(RestaurantShortDTO::new).toList();
    }


    @GetMapping("/similarProducts")
    public List<FoodDTO> getSimilarFood(@RequestParam(required = false) Long categoryID){
        List<Food> foodList;
        if(categoryID==null)
            foodList = foodService.Food();
        else
            foodList = foodService.getSimilarFood(categoryID);

        return foodList.stream().map(FoodDTO::new).toList();
    }

    @GetMapping("/{restaurantId}")
    public RestaurantDTO getRestaurantDetails(@PathVariable long restaurantId){
         Restaurant restaurant= restaurantService.getRestaurantProfile(restaurantId);
        return new RestaurantDTO(restaurant);
    }

    @GetMapping("/{restaurantId}/allFood")
    public List<FoodDTO> getAllRestaurantFood(@PathVariable long restaurantId){
        List<Food> foodList = foodService.restaurantFood(restaurantId);
        Collections.shuffle(foodList);
        return foodList.stream().map(FoodDTO::new).toList();
    }

    @GetMapping("/{restaurantId}/food")//just 4 food Items to be displayed with the short profile
    public List<FoodDTO> getSomeRestaurantFood(@PathVariable long restaurantId){
        List<Food> foodList = foodService.restaurantFood(restaurantId);
        Collections.shuffle(foodList);
        foodList = foodList.stream().limit(4).collect(Collectors.toList());
        return foodList.stream().map(FoodDTO::new).toList();
    }

}
