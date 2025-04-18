package com.savorybites.backend.api.publics.landingPage;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.api.dto.food.FoodDTO;
import com.savorybites.backend.api.dto.restaurant.RestaurantShortDTO;
import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.CategoryService;
import com.savorybites.backend.service.FoodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/landingPage")
public class LandingPageController {


    private final FoodService foodService;
    private final CategoryService categoryService;

    public LandingPageController(FoodService foodService, CategoryService categoryService) {
        this.foodService = foodService;
        this.categoryService = categoryService;
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
        return  null;
    }





}
