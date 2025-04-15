package com.savorybites.backend.repository;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.OrderItem;
import com.savorybites.backend.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findAllByUser(LocalUser user);

    Optional<Orders> findOrderByIdAndUser(Long id, LocalUser user);
}