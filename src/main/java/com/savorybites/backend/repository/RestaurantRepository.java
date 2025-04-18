package com.savorybites.backend.repository;

import com.savorybites.backend.model.Food;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByEmailIgnoreCase(String email);

    Optional<Restaurant> findRestaurantById(Long id);

    List<Restaurant> findAllByIsApproved(Boolean isApproved);

    List<Restaurant> findAllByStatus(Status status);

    long countByStatus(Status status);
}