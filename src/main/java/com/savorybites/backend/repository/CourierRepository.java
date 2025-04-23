package com.savorybites.backend.repository;

import com.savorybites.backend.model.Courier;
import com.savorybites.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findAllByRestaurant(Restaurant restaurant);

    List<Courier> findAllByRestaurantAndIsAvailableTrue(Restaurant restaurant);

    Optional<Courier> findByIdAndRestaurant(Long id, Restaurant restaurant);
}