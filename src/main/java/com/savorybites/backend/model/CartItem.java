package com.savorybites.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long itemID;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;


    @ManyToOne(optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name="quantity")
    private Integer quantity;

    @Column(name="total")
    private double totalPrice;

    public void increment(){
        quantity+=1;
    }

    public void increment(Integer qte){
        quantity+=qte;
    }

    public void decrement(){
        if(quantity-1<=0)
            quantity=0;
        else
            quantity-=1;
    }

    @PreUpdate
    protected void setTotalPrice(){
        totalPrice=food.getDiscountedPrice()*quantity;
    }

    @PrePersist
    protected void onCreate() {
        if(quantity == null)
            quantity = 1;
        totalPrice=food.getDiscountedPrice()*quantity;
    }

}