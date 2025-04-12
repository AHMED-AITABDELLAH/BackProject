package com.savorybites.backend.repository;

import com.savorybites.backend.model.Cart;
import com.savorybites.backend.model.CartItem;
import com.savorybites.backend.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByCart(Cart cart);

    Optional<CartItem> findByCartAndFood(Cart cart, Food food);


    Optional<CartItem> findByItem_idAndCart(Long item_id, Cart cart);


    void deleteByItem_id(long itemID);
}