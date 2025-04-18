package com.savorybites.backend.api.dto.admin;

import com.savorybites.backend.api.dto.restaurant.RestaurantDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
public class RestaurantStatistics {
    private long totalRestaurantCount;
    private long bannedRestaurantCount;
    private long approvedRestaurantCount;
    private long verifiedRestaurantCount;
    private long declinedRestaurantCount;
    private long pendingRestaurantCount;
    private List<RestaurantDTO> mostPopularRestaurants;


}
