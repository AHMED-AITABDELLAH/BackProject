package com.savorybites.backend.api.dto.Order;

import com.savorybites.backend.api.dto.address.AddressShortDTO;
import com.savorybites.backend.api.dto.restaurant.RestaurantShortDTO;
import com.savorybites.backend.model.Courier;
import com.savorybites.backend.model.OrderItem;
import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OrderDTO {
    private Long id;
    private Long userId;
    private RestaurantShortDTO restaurant;
    private List<OrderItemDTO> items;
    private AddressShortDTO deliveryAddress;
    private LocalDateTime orderDate;
    private Courier courier;
    private double totalPrice;
    private boolean isPaid;
    private LocalDateTime paymentDate;
    private OrderStatus status;

    public OrderDTO(Orders order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.restaurant = new RestaurantShortDTO(order.getRestaurant());
        this.items =new ArrayList<>();
        for(OrderItem orderItem :order.getItems())
            items.add(new OrderItemDTO(orderItem));

        if(order.getDeliveryAddress()!=null)
            this.deliveryAddress = new AddressShortDTO(order.getDeliveryAddress());
        else
            this.deliveryAddress=null;
        this.courier= order.getCourier();
        this.orderDate = order.getOrderDate();
        this.totalPrice = order.getTotalPrice();
        this.isPaid = order.isPaid();
        this.paymentDate = order.getPaymentDate();
        this.status = order.getStatus();
    }
}
