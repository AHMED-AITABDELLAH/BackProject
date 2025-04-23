package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.restaurant.CourierRegistrationBody;
import com.savorybites.backend.model.Courier;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.TrackingData;
import com.savorybites.backend.repository.CourierRepository;
import com.savorybites.backend.repository.OrdersRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.TrackingDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourierService {

    private final CourierRepository courierRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrdersRepository ordersRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TrackingDataRepository trackingDataRepository;


    public CourierService(CourierRepository courierRepository, RestaurantRepository restaurantRepository, OrdersRepository ordersRepository, SimpMessagingTemplate messagingTemplate, TrackingDataRepository trackingDataRepository) {
        this.courierRepository = courierRepository;
        this.restaurantRepository = restaurantRepository;
        this.ordersRepository = ordersRepository;
        this.messagingTemplate = messagingTemplate;
        this.trackingDataRepository = trackingDataRepository;
    }

    public List<Courier> getAllCouriers(Restaurant restaurant) {
        return courierRepository.findAllByRestaurant(restaurant);
    }

    public List<Courier> getAvailableCouriers(Restaurant restaurant) {
        return courierRepository.findAllByRestaurantAndIsAvailableTrue(restaurant);
    }

    @Transactional
    public ResponseEntity<?> addCourier(Restaurant restaurant, CourierRegistrationBody body) {
        Restaurant managedRestaurant = restaurantRepository.findRestaurantById(restaurant.getId()).orElseThrow();
        Courier courier = new Courier();
        courier.setFullName(body.getFullName());
        courier.setPhone(body.getPhone());
        courier.setEmail(body.getEmail());
        courier.setProfileImg(body.getProfileImg());
        courier.setRestaurant(managedRestaurant);
        courierRepository.save(courier);
        return ResponseEntity.ok("Courier added successfully.");
    }

    @Transactional
    public ResponseEntity<?> deleteCourier(Restaurant restaurant, long courierID) {
        Courier courier = courierRepository.findByIdAndRestaurant(courierID,restaurant).orElseThrow();
        courierRepository.delete(courier);
        return ResponseEntity.ok("The courier have been deleted successfully");
    }

    @Transactional
    public ResponseEntity<?> assignCourier(Restaurant restaurant, long orderID, long courierID) {
        Courier courier = courierRepository.findByIdAndRestaurant(courierID,restaurant).orElseThrow();
        if(!courier.getIsAvailable())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The Courier is not available at the moment");
        Orders order = ordersRepository.findByIdAndRestaurant(orderID,restaurant).orElseThrow();
        if(order.getCourier()!=null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The order has already a courier");
        order.setCourier(courier);
        ordersRepository.save(order);
        courier.setIsAvailable(false);
        courierRepository.save(courier);
        TrackingData trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
        trackingData.setCourier(courier);
        trackingDataRepository.save(trackingData);
        //Send Message to the user that his order was assigned to the courier
        messagingTemplate.convertAndSend(
                "/topic/user/" + order.getUser().getId() + "/ordersUpdate",
                "Your Order (ID : "+order.getId()+") got a courier assigned.");
        return ResponseEntity.ok("Courier assigned successfully.");
    }

    @Transactional
    public ResponseEntity<?> updateCourier(Restaurant restaurant, long courierID,CourierRegistrationBody body) {
        Courier courier = courierRepository.findByIdAndRestaurant(courierID, restaurant).orElseThrow();
        courier.setFullName(body.getFullName());
        courier.setEmail(body.getEmail());
        courier.setPhone(body.getPhone());
        courier.setProfileImg(body.getProfileImg());
        courier.setIsAvailable(body.getIsAvailable());
        courierRepository.save(courier);
        return ResponseEntity.ok("Courier updated successfully.");
    }
}
