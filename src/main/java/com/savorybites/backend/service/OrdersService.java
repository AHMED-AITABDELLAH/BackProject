package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.Order.TrackingDataDTO;
import com.savorybites.backend.model.*;
import com.savorybites.backend.repository.CartItemRepository;
import com.savorybites.backend.repository.OrderItemRepository;
import com.savorybites.backend.repository.OrdersRepository;
import com.savorybites.backend.repository.TrackingDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdersService {


    private final CartService cartService;
    private final OrdersRepository ordersRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressService addressService;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final TrackingDataRepository trackingDataRepository;

    public OrdersService(CartService cartService,
                         OrdersRepository ordersRepository,
                         CartItemRepository cartItemRepository,
                         AddressService addressService, OrderItemRepository orderItemRepository, UserService userService, SimpMessagingTemplate messagingTemplate, EmailService emailService, TrackingDataRepository trackingDataRepository) {
        this.cartService = cartService;
        this.ordersRepository = ordersRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressService = addressService;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
        this.trackingDataRepository = trackingDataRepository;
    }


    /*User Exclusive*/

    @Transactional
    public ResponseEntity<String> createOrdersFromCart(LocalUser user) {

        //Getting the cart associated with the user
        Cart cart= cartService.getManagedCart(user);
        //Getting the cartItems associated with the cart
        List<CartItem> cartItemList=cartItemRepository.findAllByCart(cart);
        if(cartItemList.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Cart is empty");

        //Group the cartItems by the restaurant and stores them in a map with the key is a restaurant
        Map<Restaurant, List<CartItem>> grouped = cartItemList.stream()
                .collect(Collectors
                        .groupingBy(cartItem -> cartItem.getFood().getRestaurant()));

        //Creating the list of order
        List<Orders> placedOrders = new ArrayList<>();

        //Iterating on the created map to fill our placedOrder with data
        for (Map.Entry<Restaurant, List<CartItem>> entry : grouped.entrySet()) {
            Restaurant restaurant = entry.getKey();
            List<CartItem> cartItems = entry.getValue();

            //Creating an order
            Orders orders = new Orders();
            orders.setUser(user);
            orders.setRestaurant(restaurant);
            orders.setOrderDate(LocalDateTime.now());
            orders.setStatus(OrderStatus.UNCOMPLETED);

            List<OrderItem> orderItems = new ArrayList<>();
            double total = 0;

            //initiation of the OrderItems and calculating the total
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem(cartItem, orders);
                orderItems.add(orderItem);
                total += cartItem.getTotalPrice();
            }
            total+= restaurant.getShippingFees();

            //Assigning the rest of the data to the order
            //TODO TREATING THE PAYMENT
            orders.setItems(orderItems);
            orders.setTotalPrice(total);
            //Adding the order to the Orders List aka placedOrders
            placedOrders.add(orders);
        }

        // Save all orders (cascade saves OrderItems)
        placedOrders=ordersRepository.saveAll(placedOrders);
        for(Orders order:placedOrders){
            TrackingData trackingData = new TrackingData();
            trackingData.setOrder(order);
            trackingDataRepository.save(trackingData);
        }
        // Clear cart
        cartItemRepository.deleteAll(cartItemList);


        //Alert the user that he has orders to complete
        messagingTemplate.convertAndSend(
                "/topic/user/" + user.getId() + "/orders",
                "Your orders have been created." +
                        "\nPlease proceed to checkout to complete your purchases."
        );

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> continueCheckout(LocalUser user,long addressID,long orderID) {

            Orders order = ordersRepository.findById(orderID)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // Ensure the order belongs to the user and is in the right status
            if (
                    !Objects.equals(order.getUser().getId(), user.getId())
                            ||
                    !order.getStatus().equals(OrderStatus.UNCOMPLETED)
            )
                throw new SecurityException("Order not accessible for checkout");

            // Assign address and payment method
            order.setDeliveryAddress(addressService.getAddress(user,addressID));
            //TODO PAYMENT

            order.setStatus(OrderStatus.COMPLETED);

            ordersRepository.save(order);

            TrackingData trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
            trackingData.setCompletedAt(LocalDateTime.now());
            trackingDataRepository.save(trackingData);
            //Send Notification to the restaurant
            Long restaurantId = order.getRestaurant().getId();
            // You might want to customize the payload (full order, DTO, just IDs, etc)
            messagingTemplate.convertAndSend(
                    "/topic/restaurant/" + restaurantId + "/orders",
                    "New Order has arrived (ID : "+order.getId()+")");
            return ResponseEntity.ok("Your Order is completed successfully.");
        }

    public List<Orders> getPlacedOrders(LocalUser user){
        List<Orders> ordersList = ordersRepository.findAllByUser(userService.getManagedUser(user));
        if(ordersList==null)
            throw new RuntimeException("Something went wrong");
        return ordersList;
    }

    public List<OrderItem> getOrderItems(LocalUser user,long orderID){
        Orders order=getManagedOrder(user,orderID);
        List<OrderItem> orderItems = orderItemRepository.findAllByOrders(order);
        if(orderItems==null)
            throw new RuntimeException("Something went wrong");
        return orderItems;
    }

    @Transactional
    public boolean cancelOrder(LocalUser user,long orderID){
        Orders order=getManagedOrder(user,orderID);
        if(order.getStatus()==OrderStatus.UNCOMPLETED || order.getStatus()==OrderStatus.COMPLETED) {
            order.setStatus(OrderStatus.CANCELLED);
            emailService.sendOrderCancelledByUserEmail(user.getUsername(),user.getEmail(),orderID);
            messagingTemplate.convertAndSend(
                    "/topic/restaurant/" + order.getRestaurant().getId() + "/orders",
                    "Order (ID : " + order.getId() + ") has been cancel by the user");
            //TODO TREATING THE REFUND
            return true;
        }
        return false;
    }

    private Orders getManagedOrder(LocalUser user,long orderID){
        Optional<Orders> optionalOrder = ordersRepository.findOrderByIdAndUser(orderID,user);
        if(optionalOrder.isEmpty())
            throw new RuntimeException("Something went wrong");
        return optionalOrder.get();
    }

    /*Restaurant Exclusive*/


    //this return all the orders but the one that are incompleted
    public List<Orders> getRestaurantOrderHistory(Restaurant restaurant) {
        return ordersRepository.findAllCompletedOrdersByRestaurantAndStatusIsNotIn(restaurant,List.of(OrderStatus.UNCOMPLETED,OrderStatus.COMPLETED));
    }

    public List<Orders> getRestaurantIncomingOrders(Restaurant restaurant) {
        return ordersRepository.findAllCompletedOrdersByRestaurantAndStatusIs(restaurant,OrderStatus.COMPLETED);
    }

    public List<Orders> getAllOrdersByStatusAndRestaurant(Restaurant restaurant,OrderStatus status){
        return ordersRepository.findAllByStatusAndRestaurant(status,restaurant);
    }

    @Transactional
    public ResponseEntity<?> updateStatus(Restaurant restaurant,OrderStatus orderStatus,long orderID){
        Optional<Orders> opOrder = ordersRepository.findByIdAndRestaurant(orderID,restaurant);
        if(opOrder.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Orders order= opOrder.get();
        //the order shouldn't be out for delivery if not assigned a courier
        if(orderStatus==OrderStatus.OUT_FOR_DELIVERY){
            if(order.getCourier()==null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The Orders has no courier assigned to it.");
        }
        //the order shouldn't be delivered if not assigned a courier
        if(orderStatus==OrderStatus.DELIVERED){
            if(order.getCourier()==null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The Orders has no courier assigned to it.");
            order.getCourier().setIsAvailable(true);
        }
        order.setStatus(orderStatus);
        ordersRepository.save(order);
        //Send Notification to the user
        messagingTemplate.convertAndSend(
                "/topic/user/" + order.getUser().getId() + "/ordersUpdate",
                "Your Order (ID : "+order.getId()+") got a status update ("+order.getStatus()+")");
        String status = "";
        TrackingData trackingData=new TrackingData();
        if(order.getStatus()==OrderStatus.PREPARING){
            status="Being Prepared";
            trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
            trackingData.setPreparationStartedAt(LocalDateTime.now());
        }

        else if (order.getStatus()==OrderStatus.OUT_FOR_DELIVERY) {
            status = "Out for Delivery";
            trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
            trackingData.setPickedUpAt(LocalDateTime.now());
        }
        else if (order.getStatus()==OrderStatus.DELIVERED) {
            status = "Delivered";
            trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
            trackingData.setDeliveredAt(LocalDateTime.now());
        }
        trackingDataRepository.save(trackingData);

        emailService.sendOrderProgressEmail(
                order.getUser().getUsername(),
                order.getUser().getEmail(),
                orderID,
                status);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> rejectOrder(Restaurant restaurant, long orderID) {
        Orders order = ordersRepository.findByIdAndRestaurant(orderID, restaurant).orElseThrow();
        if (order.getStatus() != OrderStatus.COMPLETED)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can't decline this order.");
        order.setStatus(OrderStatus.REJECTED);
        ordersRepository.save(order);
        messagingTemplate.convertAndSend(
                "/topic/user/" + order.getUser().getId() + "/ordersUpdate",
                "Your Order (ID : " + order.getId() + ") has been rejected by the restaurant");
        emailService.sendOrderRejectedByRestaurantEmail(
                order.getUser().getUsername(),
                order.getUser().getEmail(),
                orderID,
                order.getRestaurant().getTitle());
        return ResponseEntity.ok("Order is Rejected successfully.");
    }

    public ResponseEntity<?> acceptOrder(Restaurant restaurant, long orderID) {
        Orders order = ordersRepository.findByIdAndRestaurant(orderID, restaurant).orElseThrow();
        if (order.getStatus() != OrderStatus.COMPLETED)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can't accept this order.");
        order.setStatus(OrderStatus.ACCEPTED);
        ordersRepository.save(order);
        TrackingData trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
        trackingData.setAcceptedAt(LocalDateTime.now());
        trackingDataRepository.save(trackingData);

        messagingTemplate.convertAndSend(
                "/topic/user/" + order.getUser().getId() + "/ordersUpdate",
                "Your Order (ID : " + order.getId() + ") has been accepted by the restaurant");
        emailService.sendOrderProgressEmail(
                order.getUser().getUsername(),
                order.getUser().getEmail(),
                orderID,
                "Accepted");
        return ResponseEntity.ok("Order is Accepted successfully.");
    }

    public TrackingDataDTO getTrackingData(LocalUser user, long orderID) {
        Orders order=getManagedOrder(user,orderID);
        TrackingData trackingData = trackingDataRepository.findByOrder(order).orElseThrow();
        return new TrackingDataDTO(order,trackingData);
    }
}
