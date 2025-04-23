package com.savorybites.backend.repository;

import com.savorybites.backend.model.Category;
import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.FoodStatus;
import com.savorybites.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    //this method is for the user interface


    //to get all available food item listed in a restaurant by id
    List<Food> findByRestaurantIdAndIsAvailableTrueAndIsFlaggedIsFalse(Long id);

    //this method is for the restaurant interface to get all its listed food items
    List<Food> findByRestaurantAndIsFlaggedIsFalse(Restaurant restaurant);

    //this method for showing all the available food in the data
    List<Food> findAllByIsAvailableTrueAndIsFlaggedIsFalse();
    List<Food> findAllByIsAvailableTrue();

    //this to show all the food that is popular
    //TODO add IsFlagged false
    List<Food> findByIsAvailableTrueAndIsFlaggedFalseOrderBySoldDesc();



    //this method for showing a promotion (example : discount > 10%)
    List<Food> findAllByDiscountGreaterThan(int discountIsGreaterThan);

    //this method for showing a promotion (discount > 10%)
    List<Food> findAllByDiscountGreaterThanAndIsFlaggedIsFalse(int discountIsGreaterThan);

    Optional<Food> findFoodByIdAndRestaurantAndIsFlaggedIsFalse(Long foodID, Restaurant restaurant);


    List<Food> findDistinctByCategoriesInAndIsAvailableIsTrueAndIsFlaggedIsFalse(Collection<Category> categories);

    //this method List food by a category
    List<Food> findByCategories_IdAndIsFlaggedIsFalse(Long id);

    void deleteByIdAndRestaurant(Long id, Restaurant restaurant);


    @Query("SELECT max(discountedPrice) FROM Food ORDER BY discountedPrice")
    BigDecimal findMostExpensiveFoodPrice();
    @Query("SELECT MIN(discountedPrice) FROM Food ORDER BY discountedPrice")
    BigDecimal findLeastExpensiveFoodPrice();

    @Query("SELECT max(discountedPrice) FROM Food WHERE restaurant = :restaurant ORDER BY discountedPrice")
    BigDecimal findRestaurantMostExpensiveFoodPrice(Restaurant restaurant);

    @Query("SELECT MIN(discountedPrice) FROM Food WHERE restaurant = :restaurant ORDER BY discountedPrice")
    BigDecimal findRestaurantLeastExpensiveFoodPrice(Restaurant restaurant);

    List<Food> findAllByStatus(FoodStatus status);

    List<Food> findAllByCategories(List<Category> categories);

    long countByIsFlaggedTrue();

    long countByRestaurant(Restaurant restaurant);

    long countByRestaurantAndIsFlaggedTrue(Restaurant restaurant);

    @Query(value = """
    SELECT r.* FROM RESTAURANT r
    JOIN FOOD f ON r.id = f.restaurant_id
    GROUP BY r.id, r.title, r.description, r.email, r.phone, r.PROFILE_IMG, r.status, r.address_id, CONTACT_EMAIL, IS_APPROVED, IS_VERIFIED, PASSWORD, PAYPAL_EMAIL, PROFILE_BANNER, ROLE, SHIPPING_FEES
    ORDER BY SUM(f.sold) DESC
    FETCH FIRST 3 ROWS ONLY
    """, nativeQuery = true)
    List<Restaurant> findTop3RestaurantsByFoodSold();

    @Query(value = """
    SELECT * FROM FOOD
    ORDER BY sold DESC
    FETCH FIRST 3 ROWS ONLY
    """, nativeQuery = true)
    List<Food> findTop3BestSellingFoodsGlobally();

    @Query(value = """
    SELECT * FROM FOOD
    WHERE restaurant_id = :restaurantId
    ORDER BY sold DESC
    FETCH FIRST 3 ROWS ONLY
    """, nativeQuery = true)
    List<Food> findTop3BestSellingFoodsByRestaurant(@Param("restaurantId") Long restaurantId);


}