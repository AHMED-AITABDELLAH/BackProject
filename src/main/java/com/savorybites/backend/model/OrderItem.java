package com.savorybites.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;



    @Column(name = "price")
    private double priceAtOrderTime;

    @Column(name = "quantity")
    private int quantity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;


    public OrderItem(CartItem cartItem,Orders orders){
        priceAtOrderTime=cartItem.getTotalPrice();
        quantity=cartItem.getQuantity();
        food=cartItem.getFood();
        this.orders = orders;

    }
    public OrderItem() {
    }


}