package com.savorybites.backend.api.dto.food;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.savorybites.backend.model.Food;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class FoodRegistrationBody {
    //TODO: ADD CATEGORIES
    private String title;
    private String description;
    private String image;
    private double price;
    private int discount;
    private Boolean isAvailable;
    //Ignoring this when giving response
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Long> categoriesID;


    public FoodRegistrationBody() {}

    protected FoodRegistrationBody(Food food){
        this.title = food.getTitle();
        this.description = food.getDescription();
        this.image = food.getImage();
        this.price = food.getPrice();
        this.discount = food.getDiscount();
        this.isAvailable = food.getIsAvailable();
    }

}