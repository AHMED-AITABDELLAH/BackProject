package com.savorybites.backend.api.webhook;

import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.repository.OrdersRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
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
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Received Stripe event: {}", event.getType());
            log.info("Stripe Version : {}", Stripe.API_VERSION);
            log.info("Api verison ;{}",event.getApiVersion());
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    return handlePaymentIntentSucceeded(event);
                case "payment_intent.payment_failed":
                    return handlePaymentIntentFailed(event);
                case "charge.succeeded":
                    return handleChargeSucceeded(event);
                case "charge.failed":
                    return handleChargeFailed(event);
                default:
                    log.info("Unhandled event type: {}", event.getType());
                    return ResponseEntity.ok("Unhandled event type");
            }
        } catch (StripeException e) {
            log.error("Stripe API error", e);
            return ResponseEntity.internalServerError().body("Stripe API error");
        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.internalServerError().body("Processing error");
        }
    }

    private ResponseEntity<String> handlePaymentIntentSucceeded(Event event) throws StripeException {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
        return updateOrderFromPaymentIntent(paymentIntent, OrderStatus.COMPLETED, "Payment succeeded");
    }

    private ResponseEntity<String> handlePaymentIntentFailed(Event event) throws StripeException {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
        return updateOrderFromPaymentIntent(paymentIntent, OrderStatus.PAYMENT_FAILED, "Payment failed");
    }

    private ResponseEntity<String> handleChargeSucceeded(Event event) throws StripeException {
        Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElseThrow();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(charge.getPaymentIntent());
        return updateOrderFromPaymentIntent(paymentIntent, OrderStatus.COMPLETED, "Charge succeeded");
    }

    private ResponseEntity<String> handleChargeFailed(Event event) throws StripeException {
        Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElseThrow();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(charge.getPaymentIntent());
        return updateOrderFromPaymentIntent(paymentIntent, OrderStatus.PAYMENT_FAILED, "Charge failed");
    }

    private ResponseEntity<String> updateOrderFromPaymentIntent(
            PaymentIntent paymentIntent,
            OrderStatus status,
            String successMessage) {

        // Try to find order by PaymentIntent ID first
        Orders order = ordersRepository.findByPaymentIntentId(paymentIntent.getId())
                .or(() -> {
                    // Fallback to metadata if PaymentIntent ID not found
                    String orderId = paymentIntent.getMetadata().get("orderId");
                    if (orderId != null) {
                        return ordersRepository.findById(Long.parseLong(orderId));
                    }
                    return Optional.empty();
                })
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        ordersRepository.save(order);

        // Notify restaurant
        messagingTemplate.convertAndSend(
                "/topic/restaurant/" + order.getRestaurant().getId() + "/orders",
                "Order update: " + order.getId() + " - " + status);

        log.info("Updated order {} to status {}", order.getId(), status);
        return ResponseEntity.ok(successMessage);
    }
}