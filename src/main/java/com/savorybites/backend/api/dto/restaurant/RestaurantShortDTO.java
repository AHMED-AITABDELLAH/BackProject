package com.savorybites.backend.api.dto.restaurant;

import com.savorybites.backend.model.Restaurant;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestaurantShortDTO {
    private Long id;
    private String title;


    public RestaurantShortDTO(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.title = restaurant.getTitle();
    }

}
