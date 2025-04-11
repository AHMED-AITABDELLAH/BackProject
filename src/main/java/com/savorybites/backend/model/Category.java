package com.savorybites.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "category_image")
    private String categoryImage;

    @Column(name = "category_icon")
    private String categoryIcon;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories")
    private List<Food> foodCategories = new ArrayList<>();

}