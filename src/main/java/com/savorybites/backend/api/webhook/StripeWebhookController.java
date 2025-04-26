package com.savorybites.backend.api.webhook;

import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.repository.OrdersRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stripe")
public class StripeWebhookController {

    private final OrdersRepository ordersRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.stripe.webhook.secret}")
    private String webhookSecret;

    @Transactional
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader,
                                                      @RequestHeader(value = "Stripe-Account", required = false) String stripeAccount) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        System.out.println("Received event type: " + event.getType());

        try {
            if ("payment_intent.succeeded".equals(event.getType())) {
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

                // For payment_intent.succeeded, we need to get the PaymentIntent
                Optional<StripeObject> objectOptional = dataObjectDeserializer.getObject();
                if (objectOptional.isPresent() && objectOptional.get() instanceof PaymentIntent paymentIntent) {
                    System.out.println("PaymentIntent metadata: " + paymentIntent.getMetadata());

                    String orderId = paymentIntent.getMetadata().get("orderId");
                    if (orderId != null) {
                        updateOrderStatus(orderId, OrderStatus.COMPLETED);
                        return ResponseEntity.ok("PAYMENT_SUCCESS");
                    }
                }
            } else if ("charge.succeeded".equals(event.getType())) {
                // For charge.succeeded, we need to get the Charge object and then the payment_intent ID
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                Optional<StripeObject> objectOptional = dataObjectDeserializer.getObject();

                if (objectOptional.isPresent()) {
                    Object object = objectOptional.get();
                    String paymentIntentId = null;

                    if (object instanceof Charge charge) {
                        paymentIntentId = charge.getPaymentIntent();
                    }

                    if (paymentIntentId != null) {
                        // Retrieve the PaymentIntent to get the metadata
                        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                        String orderId = paymentIntent.getMetadata().get("orderId");

                        if (orderId != null) {
                            updateOrderStatus(orderId, OrderStatus.COMPLETED);
                            return ResponseEntity.ok("PAYMENT_SUCCESS");
                        }
                    }
                }
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                // Handle payment failure (existing code)
            }

            // For all other event types, return OK but don't process
            return ResponseEntity.ok("EVENT_NOT_HANDLED");

        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    private void updateOrderStatus(String orderId, OrderStatus status) {
        Orders order = ordersRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        ordersRepository.save(order);

        messagingTemplate.convertAndSend(
                "/topic/restaurant/" + order.getRestaurant().getId() + "/orders",
                "New Order has arrived (ID : " + order.getId() + ")");
    }
}