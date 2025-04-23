package com.savorybites.backend.api.dto.Order;

import com.savorybites.backend.api.dto.address.AddressShortDTO;
import com.savorybites.backend.model.Courier;
import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.model.TrackingData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@NoArgsConstructor
public class TrackingDataDTO {

    private Long id;

    private OrderStatus status;

    private LocalDateTime placedAt;

    private LocalDateTime completedAt;

    private AddressShortDTO pickupLocation;

    private LocalDateTime acceptedAt;

    private LocalDateTime preparationStartedAt;

    private LocalDateTime pickedUpAt;

    private LocalDateTime deliveredAt;

    private AddressShortDTO deliveryLocation;

    private Courier courier;

    private OrderShortDTO orderShortDTO;

    public TrackingDataDTO(Orders order, TrackingData trackingData){
        this.id = order.getId();
        this.status = order.getStatus();
        this.placedAt = order.getOrderDate();
        this.completedAt = trackingData.getCompletedAt();
        this.acceptedAt = trackingData.getAcceptedAt();
        this.pickupLocation=new AddressShortDTO(order.getRestaurant().getAddress());
        this.deliveryLocation=new AddressShortDTO(order.getDeliveryAddress());
        this.preparationStartedAt = trackingData.getPreparationStartedAt();
        this.pickedUpAt = trackingData.getPickedUpAt();
        this.deliveredAt = trackingData.getDeliveredAt();
        this.courier = trackingData.getCourier();
        this.orderShortDTO = new OrderShortDTO(order);
    }
}
