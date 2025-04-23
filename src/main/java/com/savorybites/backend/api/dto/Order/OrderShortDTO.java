package com.savorybites.backend.api.dto.Order;

import com.savorybites.backend.api.dto.restaurant.RestaurantShortDTO;
import com.savorybites.backend.model.OrderItem;
import com.savorybites.backend.model.Orders;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class OrderShortDTO {
    private Long id;
    private RestaurantShortDTO restaurant;
    private List<OrderItemDTO> items;
    private double totalPrice;

    public OrderShortDTO(Orders order) {
        this.id = order.getId();
        this.restaurant = new RestaurantShortDTO(order.getRestaurant());
        this.items =new ArrayList<>();
        for(OrderItem orderItem :order.getItems())
            items.add(new OrderItemDTO(orderItem));
        this.totalPrice = order.getTotalPrice();
    }
}
