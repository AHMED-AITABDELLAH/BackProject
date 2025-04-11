package com.savorybites.backend.repository;

import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByEmailIgnoreCase(String email);

    Optional<Restaurant> findRestaurantById(Long id);

}