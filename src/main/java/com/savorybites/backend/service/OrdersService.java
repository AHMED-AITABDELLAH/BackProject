package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.Order.OrderDTO;
import com.savorybites.backend.model.*;
import com.savorybites.backend.repository.CartItemRepository;
import com.savorybites.backend.repository.OrderItemRepository;
import com.savorybites.backend.repository.OrdersRepository;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Transactional
    public List<Orders> createOrdersFromCart(LocalUser user)
    {

        //Getting the cart associated with the user
        Cart cart= cartService.getManagedCart(user);
        //Getting the cartItems associated with the cart
        List<CartItem> cartItemList=cartItemRepository.findAllByCart(cart);
        if(cartItemList.isEmpty())
            throw new RuntimeException("The cart is empty!");


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
            orders.setDeliveryAddress(addressService.getDefaultAddress(user)); // adapt this
            orders.setStatus(OrderStatus.PENDING);

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

        for (Orders order : placedOrders) {
            Long restaurantId = order.getRestaurant().getId();
            // You might want to customize the payload (full order, DTO, just IDs, etc)
            messagingTemplate.convertAndSend(
                    "/topic/restaurant/" + restaurantId + "/orders",
                    "New Order has arrived (ID:)"+order.getId() // or create an OrderNotification DTO if you like
            );
        }

        return placedOrders;
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
        if(order.getStatus()==OrderStatus.DELIVERED || order.getStatus()==OrderStatus.CANCELLED)
            return false;
        order.setStatus(OrderStatus.CANCELLED);
        //TODO TREATING THE REFUND
        return true;
    }

    private Orders getManagedOrder(LocalUser user,long orderID){
        Optional<Orders> optionalOrder = ordersRepository.findOrderByIdAndUser(orderID,user);
        if(optionalOrder.isEmpty())
            throw new RuntimeException("Something went wrong");
        return optionalOrder.get();
    }


}
