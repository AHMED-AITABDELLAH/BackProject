package com.savorybites.backend.api.dto.Order;

import com.savorybites.backend.api.dto.food.FoodDTO;
import com.savorybites.backend.model.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemDTO {
    private Long id;
    private double priceAtOrderTime;
    private int quantity;
    private FoodDTO food;


    public OrderItemDTO(OrderItem orderItem){
        this.id = orderItem.getId();
        this.priceAtOrderTime = orderItem.getPriceAtOrderTime();
        this.quantity = orderItem.getQuantity();
        this.food=new FoodDTO(orderItem.getFood());

    }

}
