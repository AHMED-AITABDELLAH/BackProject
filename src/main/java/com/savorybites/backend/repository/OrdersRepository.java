package com.savorybites.backend.repository;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findAllByUser(LocalUser user);

    Optional<Orders> findOrderByIdAndUser(Long id, LocalUser user);

    @Query(value = """
    SELECT
      TO_CHAR(o.ORDER_DATE, 'IYYY-"W"IW') AS week_label,
      COUNT(CASE WHEN o.status = 'DELIVERED' THEN 1 END) AS deliveredOrdersCount,
      COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) AS canceledOrdersCount,
      COUNT(CASE WHEN o.status = 'DECLINED' THEN 1 END) AS declinedOrdersCount
    FROM orders o
    GROUP BY TO_CHAR(o.ORDER_DATE, 'IYYY-"W"IW')
    ORDER BY TO_CHAR(o.ORDER_DATE, 'IYYY-"W"IW')
    """, nativeQuery = true)
    List<Object[]> findWeeklyOrderStats();


    @Query(value = """
    SELECT TO_CHAR(o.order_date, 'YYYY-MM') AS month,
           COALESCE(SUM(o.total_price), 0) AS total
    FROM orders o
    WHERE o.status = 'DELIVERED'
    GROUP BY TO_CHAR(o.order_date, 'YYYY-MM')
    ORDER BY TO_CHAR(o.order_date, 'YYYY-MM')
    """, nativeQuery = true)
    List<Object[]> findMonthlyRevenueRaw();


    @Query(value = """
    SELECT COALESCE(SUM(o.total_price), 0) Total_Revenue
    FROM orders o
    WHERE o.status = 'DELIVERED'
   """, nativeQuery = true)
    BigDecimal findTotalRevenue();

}