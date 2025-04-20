package com.savorybites.backend.service;

import com.savorybites.backend.model.*;
import com.savorybites.backend.repository.CartItemRepository;
import com.savorybites.backend.repository.OrderItemRepository;
import com.savorybites.backend.repository.OrdersRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
    public OrdersService(CartService cartService,
                         OrdersRepository ordersRepository,
                         CartItemRepository cartItemRepository,
                         AddressService addressService, OrderItemRepository orderItemRepository, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.cartService = cartService;
        this.ordersRepository = ordersRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressService = addressService;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
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
        ordersRepository.saveAll(placedOrders);

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


    public List<Orders> getRestaurantOrderHistory(Restaurant restaurant) {
        return ordersRepository.findAllCompletedOrdersByRestaurant(restaurant.getId());
    }

    public List<Orders> getAllOrdersByStatusAndRestaurant(Restaurant restaurant,OrderStatus status){
        return ordersRepository.findAllByStatusAndRestaurant(status,restaurant);
    }




}
