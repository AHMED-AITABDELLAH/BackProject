package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.api.dto.Order.OrderItemDTO;
import com.savorybites.backend.api.dto.cart.CartItemDTO;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.OrderItem;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.service.OrdersService;
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
    public List<OrderDTO> placeOrders(@AuthenticationPrincipal LocalUser user){
        List<Orders> ordersList = ordersService.createOrdersFromCart(user);
        return ordersList.stream().map(OrderDTO::new).toList();
    }

    @GetMapping("/placedOrders")
    public List<OrderDTO> placedOrders(@AuthenticationPrincipal LocalUser user){
        List<Orders> ordersList = ordersService.getPlacedOrders(user);
        return ordersList.stream().map(OrderDTO::new).toList();
    }

    @GetMapping("/{orderID}/OrderItems")
    public List<OrderItemDTO> OrderItems(@AuthenticationPrincipal LocalUser user,
                                         @PathVariable long orderID){
        List<OrderItem> orderItemList = ordersService.getOrderItems(user,orderID);
        return orderItemList.stream().map(OrderItemDTO::new).toList();
    }

    @PutMapping("/{orderID}/cancel")
    public ResponseEntity<Object> cancelOrder(@AuthenticationPrincipal LocalUser user,
                                              @PathVariable long orderID){
        boolean success= ordersService.cancelOrder(user, orderID);
        return success
                ? ResponseEntity.ok("The order is cancelled,you're refund is getting treated.")
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
