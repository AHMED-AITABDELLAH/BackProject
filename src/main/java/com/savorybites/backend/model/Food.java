package com.savorybites.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "food")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "title")
    private String title;

    @Column(name="image")
    private String image;

    @Column(name="description")
    private String description;

    @Column(name="price")
    private double price;

    @Column(name="discount")
    private int discount;

    @Column(name="discounted_price")
    private double discountedPrice;


    @Column(name="is_available")
    private Boolean isAvailable;

    @ManyToMany
    @JoinTable(name = "food_categories",
            joinColumns = @JoinColumn(name = "food_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


    @PreUpdate
    protected void setDiscountedPrice(){
        discountedPrice =price-(price*discount)/100;
    }

    @PrePersist
    protected void onCreate(){
        if(isAvailable==null)
            isAvailable=true;
        discountedPrice =price-(price*discount)/100;
    }



}