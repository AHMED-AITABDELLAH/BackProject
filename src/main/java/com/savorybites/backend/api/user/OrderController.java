package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.api.dto.Order.OrderItemDTO;
import com.savorybites.backend.api.dto.Order.TrackingDataDTO;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.OrderItem;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.model.TrackingData;
import com.savorybites.backend.service.OrdersService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/orders")
public class OrderController {


    private final OrdersService ordersService;

    public OrderController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/placeOrders")
    public ResponseEntity<String> placeOrders(@AuthenticationPrincipal LocalUser user){
         return ordersService.createOrdersFromCart(user);
    }



    @GetMapping("/placedOrders")
    public List<OrderDTO> placedOrders(@AuthenticationPrincipal LocalUser user){
        List<Orders> ordersList = ordersService.getPlacedOrders(user);
        return ordersList.stream().map(OrderDTO::new).toList();
    }

    @PutMapping("/placedOrders/{orderID}/checkout")
    public ResponseEntity<String> continueCheckout(@AuthenticationPrincipal LocalUser user,
                                                   @PathVariable long orderID,
                                                   @RequestParam long addressID,
                                                   @RequestParam long paymentMethodID)
    {
        return ordersService.continueCheckout(user,addressID,orderID,paymentMethodID);
    }

    @GetMapping("/placedOrders/{orderID}/orderItems")
    public List<OrderItemDTO> orderItems(@AuthenticationPrincipal LocalUser user,
                                         @PathVariable long orderID){
        List<OrderItem> orderItemList = ordersService.getOrderItems(user,orderID);
        return orderItemList.stream().map(OrderItemDTO::new).toList();
    }

    @PutMapping("/placedOrders/{orderID}/cancel")
    public ResponseEntity<Object> cancelOrder(@AuthenticationPrincipal LocalUser user,
                                              @PathVariable long orderID) throws StripeException {
        boolean success= ordersService.cancelOrder(user, orderID);
        return success
                ? ResponseEntity.ok("The order is cancelled,you're refund is getting treated.")
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping("/track")
    public TrackingDataDTO getTrackingData(@AuthenticationPrincipal LocalUser user,
                                           @RequestParam long orderID){
        return ordersService.getTrackingData(user,orderID);
    }
}
