package com.savorybites.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "food")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
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

    //TODO: ADD CATEGORIES


    @Column(name="isAvailable")
    private Boolean isAvailable;


    @PrePersist
    protected void onCreate(){
        if(isAvailable==null)
            isAvailable=true;
    }



}