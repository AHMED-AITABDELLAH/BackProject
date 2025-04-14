package com.savorybites.backend.api.dto.food;

import com.savorybites.backend.api.dto.restaurant.RestaurantShortDTO;
import com.savorybites.backend.model.Food;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FoodDTO {
    private Long id;
    private String title;
    private String image;
    private double discountedPrice;
    private RestaurantShortDTO restaurant;

    public FoodDTO(Food food) {
        this.id = food.getId();
        this.title = food.getTitle();
        this.image = food.getImage();
        this.discountedPrice = food.getDiscountedPrice();
        this.restaurant = new RestaurantShortDTO(food.getRestaurant());
    }
}
