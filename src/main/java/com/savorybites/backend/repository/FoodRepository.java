package com.savorybites.backend.repository;

import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
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

    //this to show all the food that is popular
    List<Food> findByIsAvailableTrueOrderBySoldDesc();



    //this method for showing a promotion (example : discount > 10%)
    List<Food> findAllByDiscountGreaterThan(int discountIsGreaterThan);

    Optional<Food> findFoodByIdAndRestaurant(Long foodID, Restaurant restaurant);





    //this method to List food by multiple categories
    List<Food> findDistinctByCategoriesInAndIsAvailableIsTrue(Collection<Category> categories);

    //this method List food by a category
    List<Food> findByCategories_Id(Long id);

    void deleteByIdAndRestaurant(Long id, Restaurant restaurant);

}