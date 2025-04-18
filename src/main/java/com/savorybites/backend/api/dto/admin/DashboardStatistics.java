package com.savorybites.backend.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
public class DashboardStatistics {

    private UserStatistics userStatistics;
    private RestaurantStatistics restaurantStatistics;
    private FoodStatistics foodStatistics;
    private OrderStatistics orderStatistics;
    private RevenueStatistics revenueStatistics;

}


