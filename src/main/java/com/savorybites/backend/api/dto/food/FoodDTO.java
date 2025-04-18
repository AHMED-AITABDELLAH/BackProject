package com.savorybites.backend.api.dto.food;

import com.savorybites.backend.api.dto.restaurant.RestaurantShortDTO;
import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class FoodDTO {
    private Long id;
    private String title;
    private String image;
    private double discountedPrice;
    private RestaurantShortDTO restaurant;
    private long sold;
    private List<String> categoryTitles;

    public FoodDTO(Food food) {
        this.id = food.getId();
        this.title = food.getTitle();
        this.image = food.getImage();
        this.discountedPrice = food.getDiscountedPrice();
        this.restaurant = new RestaurantShortDTO(food.getRestaurant());
        this.sold= food.getSold();
        this.categoryTitles=new ArrayList<>();
        for(Category category:food.getCategories())
            categoryTitles.add(category.getTitle());
    }
}
