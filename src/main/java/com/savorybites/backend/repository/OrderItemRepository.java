package com.savorybites.backend.repository;

import com.savorybites.backend.model.OrderItem;
import com.savorybites.backend.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  List<OrderItem> findAllByOrders(Orders orders);
}