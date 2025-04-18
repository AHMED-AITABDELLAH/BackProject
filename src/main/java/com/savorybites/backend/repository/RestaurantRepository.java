package com.savorybites.backend.repository;

import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByEmailIgnoreCase(String email);

    Optional<Restaurant> findRestaurantById(Long id);

    List<Restaurant> findAllByIsApprovedIsTrue();

    List<Restaurant> findAllByStatus(Status status);

    long countByStatus(Status status);


/*
    @Query(value = """
    SELECT r.id AS restaurantId, r.TITLE AS restaurantName, 
           COALESCE(SUM(o.total_price), 0) AS totalSales
    FROM restaurant r
    LEFT JOIN orders o ON o.restaurant_id = r.id
    GROUP BY r.id, r.TITLE
    ORDER BY totalSales DESC
""", nativeQuery = true)
    List<RestaurantSalesProjection> getRestaurantSalesRanked();
*/


    @Query(value = """
    SELECT r.*
    FROM restaurant r
    JOIN (
        SELECT o.restaurant_id, SUM(o.total_price) AS total_sales
        FROM orders o
        GROUP BY o.restaurant_id
    ) sales ON sales.restaurant_id = r.id
    ORDER BY sales.total_sales DESC
""", nativeQuery = true)
    List<Restaurant> findAllRestaurantsOrderBySales();


}