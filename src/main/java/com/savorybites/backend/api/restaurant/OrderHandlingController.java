package com.savorybites.backend.api.restaurant;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.CourierService;
import com.savorybites.backend.service.OrdersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurant/orders")
public class OrderHandlingController {


    private final OrdersService ordersService;
    private final CourierService courierService;

    public OrderHandlingController(OrdersService ordersService, CourierService courierService) {
        this.ordersService = ordersService;
        this.courierService = courierService;
    }

    @GetMapping("/")
    public List<OrderDTO> getAllOrders(@AuthenticationPrincipal Restaurant restaurant,
                                       @RequestParam(required = false) OrderStatus orderStatus){
        List<Orders> ordersList;
        if(orderStatus==null)
            ordersList = ordersService.getRestaurantOrderHistory(restaurant);
        else
            ordersList = ordersService.getAllOrdersByStatusAndRestaurant(restaurant,orderStatus);
        return ordersList.stream().map(OrderDTO::new).toList();
    }

    @GetMapping ("/status")
    public OrderStatus[] getOrderStatus(){
        return new OrderStatus[]{
                OrderStatus.COMPLETED,
                OrderStatus.ACCEPTED,
                OrderStatus.PREPARING,
                OrderStatus.DELIVERED,
                OrderStatus.REJECTED,
                OrderStatus.CANCELLED,
                OrderStatus.OUT_FOR_DELIVERY};
    }


    @PutMapping("/{orderID}/updateStatus")
    public ResponseEntity<?> updateStatus(@AuthenticationPrincipal Restaurant restaurant,
                                               @RequestParam OrderStatus orderStatus,
                                               @PathVariable long orderID){
        return ordersService.updateStatus(restaurant,orderStatus,orderID);
    }

    @PutMapping("/{orderID}/assignCourier")
    public ResponseEntity<?> assignCourier(@AuthenticationPrincipal Restaurant restaurant,
                                           @PathVariable long orderID,
                                           @RequestParam long courierID){
        return courierService.assignCourier(restaurant,orderID,courierID);

    }




}
