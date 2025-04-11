package com.savorybites.backend.api.dto.food;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter

@JsonPropertyOrder({"id"})
public class FoodDetailsBody extends FoodRegistrationBody {

    private long id;
    private final List<Category> categoryList;
    public FoodDetailsBody(Food food){
        super(food);
        this.id=food.getId();
        this.categoryList=food.getCategories();
    }


}
