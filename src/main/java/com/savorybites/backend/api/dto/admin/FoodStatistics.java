package com.savorybites.backend.api.dto.admin;

import com.savorybites.backend.api.dto.food.FoodDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodStatistics {

    private long totalFoodCount;
    private long totalUnflaggedFood;
    private long totalFlaggedFood;
    private BigDecimal mostExpensiveFoodPrice;
    private BigDecimal leastExpensiveFoodPrice;
    private List<FoodDTO> topSellingFoods;

    public void setTotalUnflaggedFood(){
        totalUnflaggedFood=totalFoodCount-totalFlaggedFood;
    }


}
