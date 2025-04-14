package com.savorybites.backend.repository;

import com.savorybites.backend.model.Cart;
import com.savorybites.backend.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(LocalUser user);
}