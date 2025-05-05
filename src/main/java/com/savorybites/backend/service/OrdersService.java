package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.Order.TrackingDataDTO;
import com.savorybites.backend.model.*;
import com.savorybites.backend.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
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
    private final PlatformPaymentMethodRepository platformPaymentMethodRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        return intent.cancel();
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

            //Alert the user that he has orders to complete
            messagingTemplate.convertAndSend(
                    "/topic/user/" + user.getId() + "/orderStatusUpdate",
                    "Your order (ID: " + order.getId() + ") have been created." +
                            "\nPlease proceed to checkout to complete your purchase."
            );

        }
        // Clear cart
        cartItemRepository.deleteAll(cartItemList);




        return ResponseEntity.ok().build();
    }


    @Transactional
    public ResponseEntity<String> continueCheckout(LocalUser user, long addressID, long orderID, long paymentMethodID) {
        Orders order = ordersRepository.findById(orderID)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Validation de l'ordre
        if (!Objects.equals(order.getUser().getId(), user.getId()) ||
                !order.getStatus().equals(OrderStatus.UNCOMPLETED)) {
            throw new SecurityException("Order not accessible for checkout");
        }

        // Configuration de l'adresse et du paiement
        order.setDeliveryAddress(addressService.getAddress(user, addressID));

        PlatformPaymentMethod paymentMethod = platformPaymentMethodRepository
                .findByIdAndUser(paymentMethodID, user)
                .orElseThrow(() -> new EntityNotFoundException("No payment method with that id."));

        long amount = (long) (order.getTotalPrice() * 100);
        long fee = (long) (amount * 0.1);

        try {
            // Configuration des paramètres de PaymentIntent
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("mad")
                    .setCustomer(paymentMethod.getStripeCustomerId())
                    .setPaymentMethod(paymentMethod.getStripePaymentMethodId())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .setConfirm(true)
                    .setOffSession(true)
                    .putMetadata("orderId", order.getId().toString());

            // Configuration spécifique pour Stripe Connect
            if (order.getRestaurant().getStripeAccountId() != null) {
                paramsBuilder.setTransferData(
                        PaymentIntentCreateParams.TransferData.builder()
                                .setDestination(order.getRestaurant().getStripeAccountId())
                                .build()
                ).setApplicationFeeAmount(fee);
            }

            PaymentIntent intent = PaymentIntent.create(paramsBuilder.build());
            order.setPaymentIntentId(intent.getId());
            ordersRepository.save(order);

        } catch (StripeException e) {
            throw new RuntimeException("Stripe PaymentIntent failed: " + e.getMessage(), e);
        }

        // Mise à jour de l'ordre
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaid(true);
        order.setPaymentDate(LocalDateTime.now());
        ordersRepository.save(order);

        // Envoi des notifications
        simpMessagingTemplate.convertAndSend(
                "/topic/user/" + order.getUser().getId() + "/orderStatusUpdate",
                "Your Order (ID: " + order.getId() + ") is completed, you will not be charged until your order is delivered."
        );

        simpMessagingTemplate.convertAndSend(
                "/topic/restaurant/" + order.getRestaurant().getId() + "/orders",
                "New Order (ID: " + order.getId() + ") is received."
        );

        // Mise à jour du suivi
        TrackingData trackingData = trackingDataRepository.findByOrder(order)
                .orElseThrow(() -> new NoSuchElementException("There is no tracking data"));
        trackingData.setCompletedAt(LocalDateTime.now());
        trackingDataRepository.save(trackingData);

        return ResponseEntity.ok().build();
    }

    public List<Orders> getPlacedOrders(LocalUser user){
        List<Orders> ordersList = ordersRepository.findAllByUserOrderByOrderDateDesc(userService.getManagedUser(user));
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
    public boolean cancelOrder(LocalUser user,long orderID) throws StripeException {
        Orders order=getManagedOrder(user,orderID);
        if(order.getStatus()==OrderStatus.UNCOMPLETED ||
                order.getStatus()==OrderStatus.COMPLETED ||
                order.getStatus()==OrderStatus.ACCEPTED
        ) {

            if(order.isPaid()){
                PaymentIntent cancelledPaymentIntent = cancelPaymentIntent(order.getPaymentIntentId());
            }
            order.setStatus(OrderStatus.CANCELLED);

            emailService.sendOrderCancelledByUserEmail(user.getUsername(),user.getEmail(),orderID);
            messagingTemplate.convertAndSend(
                    "/topic/restaurant/" + order.getRestaurant().getId() + "/orders",
                    "Unfortunately, the order (ID: " + order.getId() + ") was cancelled by the client. No charges were, and the payment was not processed."
            );
            messagingTemplate.convertAndSend(
                    "/topic/user/" + order.getUser().getId() + "/orderStatusUpdate",
                    "Your order (ID: " + order.getId() + ") has been canceled. No charges were made, and your payment was not processed."
            );

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
        Transfer transfer;
        Optional<Orders> opOrder = ordersRepository.findByIdAndRestaurant(orderID,restaurant);
        if(opOrder.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Orders order= opOrder.get();
        //the order shouldn't be out for delivery if not assigned a courier
        if(orderStatus==OrderStatus.OUT_FOR_DELIVERY){
            if(order.getCourier()==null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The Orders has no courier assigned to it.");
        }
        //the order shouldn't be delivered if not assigned a courier And when delivered transfer money to restaurant
        if (orderStatus == OrderStatus.DELIVERED) {
            if (order.getCourier() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The Order has no courier assigned to it.");

            // 1. Make courier available again
            order.getCourier().setIsAvailable(true);
            try {
                // 2. We capture the payment intent
                PaymentIntent intent = PaymentIntent.retrieve(order.getPaymentIntentId());
                PaymentIntent capturedIntent = intent.capture();
            } catch (StripeException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Stripe transfer failed: " + e.getMessage());
            }
            // 3. Notify the restaurant
            messagingTemplate.convertAndSend(
                    "/topic/restaurant/" + order.getRestaurant().getId() + "/orders",
                    "The payment for the Order (ID: " + order.getId() + ") was transferred successfully.");

            // 3. Notify the admins
            messagingTemplate.convertAndSend(
                    "/topic/admin",
                    "The Application fees for the Order (ID: " + order.getId() + ") was collected successfully."
            );
        }
        order.setStatus(orderStatus);
        ordersRepository.save(order);
        //Filling the tracking data
        String status = "";
        TrackingData trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
        if(order.getStatus()==OrderStatus.PREPARING){
            status="Being Prepared";
            trackingData.setPreparationStartedAt(LocalDateTime.now());
        }
        else if (order.getStatus()==OrderStatus.OUT_FOR_DELIVERY) {
            status = "Out for Delivery";
            trackingData.setPickedUpAt(LocalDateTime.now());
        }
        else if (order.getStatus()==OrderStatus.DELIVERED) {
            status = "Delivered";
            trackingData.setDeliveredAt(LocalDateTime.now());
        }
        //save the tracking data
        trackingDataRepository.save(trackingData);
        //Send Notification to the user
        messagingTemplate.convertAndSend(
                "/topic/user/" + order.getUser().getId() + "/orderStatusUpdate",
                "Your Order (ID : "+order.getId()+") got a status update ("+order.getStatus()+")");

        //Send update email to the user
        emailService.sendOrderProgressEmail(
                order.getUser().getUsername(),
                order.getUser().getEmail(),
                orderID,
                status);

        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> rejectOrder(Restaurant restaurant, long orderID) throws StripeException {
        Orders order = ordersRepository.findByIdAndRestaurant(orderID, restaurant).orElseThrow();
        if (order.getStatus() != OrderStatus.COMPLETED)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can't decline this order.");
        order.setStatus(OrderStatus.REJECTED);

        //refund the charges
        PaymentIntent cancelledPaymentIntent = cancelPaymentIntent(order.getPaymentIntentId());

        ordersRepository.save(order);
        messagingTemplate.convertAndSend(
                "/topic/user/" + order.getUser().getId() + "/orderStatusUpdate",
                "Unfortunately, Your order (ID: " + order.getId() + ") has been rejected by the restaurant. No charges were, and your payment was not processed.."
        );
        emailService.sendOrderRejectedByRestaurantEmail(
                order.getUser().getUsername(),
                order.getUser().getEmail(),
                orderID,
                order.getRestaurant().getTitle());
        return ResponseEntity.ok("Order is Rejected successfully.");
    }

    @Transactional
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
                "/topic/user/" + order.getUser().getId() + "/orderStatusUpdate",
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
