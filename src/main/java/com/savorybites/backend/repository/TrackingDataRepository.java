package com.savorybites.backend.repository;

import com.savorybites.backend.model.Orders;
import com.savorybites.backend.model.TrackingData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingDataRepository extends JpaRepository<TrackingData, Long> {
    Optional<TrackingData> findByOrder(Orders order);
}