package com.savorybites.backend.api.dto.cart;

import com.savorybites.backend.api.dto.food.FoodDTO;
import com.savorybites.backend.model.CartItem;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CartItemDTO {
    private Long itemID;
    private FoodDTO food;
    private int quantity;
    private double totalPrice;




    public CartItemDTO(CartItem item) {
        this.itemID = item.getItemID();
        this.quantity = item.getQuantity();
        this.totalPrice = item.getTotalPrice();
        this.food = new FoodDTO(item.getFood());
    }

}
