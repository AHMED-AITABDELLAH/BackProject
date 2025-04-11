package com.savorybites.backend.api.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

}