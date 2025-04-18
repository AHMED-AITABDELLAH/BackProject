package com.savorybites.backend.api.dto.admin;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
public class OrderStatistics {

    private long totalOrderCount;
    private List<WeeklyOrderStats> weeklyOrderStats;
    private List<OrderDTO> topBigOrders;

}
