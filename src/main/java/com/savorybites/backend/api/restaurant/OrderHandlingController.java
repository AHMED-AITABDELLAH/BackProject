package com.savorybites.backend.api.restaurant;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.model.Admin;
import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.OrdersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurant/orders")
public class OrderHandlingController {


    private final OrdersService ordersService;

    public OrderHandlingController(OrdersService ordersService) {
        this.ordersService = ordersService;
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
                OrderStatus.PREPARING,
                OrderStatus.DELIVERED,
                OrderStatus.REJECTED,
                OrderStatus.CANCELLED,
                OrderStatus.OUT_FOR_DELIVERY};
    }


    @PutMapping("/{orderID}/updateStatus")
    public ResponseEntity<String> updateStatus(){
        return null;
    }



}
