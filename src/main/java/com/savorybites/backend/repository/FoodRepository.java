package com.savorybites.backend.repository;

import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    //this method is for the user interface
    //to get all available food item listed in a restaurant by id
    List<Food> findByRestaurantIdAndIsAvailableTrue(Long id);

    //this method is for the restaurant interface to get all its listed food items
    List<Food> findByRestaurant(Restaurant restaurant);

    //this method for showing all the available food in the data
    List<Food> findAllByIsAvailableTrue();

    //this method for showing a promotion (example : discount > 60%)
    List<Food> findAllByDiscountGreaterThan(int discountIsGreaterThan);

    Optional<Food> findFoodById(long foodID);

    Optional<Food> deleteFoodById(long foodID);


    //this method to List food by their categories
    //TODO: ADD CATEGORIES


}