package com.savorybites.backend.api.dto.restaurant;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class FoodStockStatistics {

    private long totalFoodCount;
    private long totalAvailableFood;
    private long totalUnavailableFood;
}
