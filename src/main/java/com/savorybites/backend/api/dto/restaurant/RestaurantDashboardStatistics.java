package com.savorybites.backend.api.dto.restaurant;

import com.savorybites.backend.api.dto.admin.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RestaurantDashboardStatistics {

    private FoodStatistics foodStatistics;
    private OrderStatistics orderStatistics;
    private RevenueStatistics revenueStatistics;

}
