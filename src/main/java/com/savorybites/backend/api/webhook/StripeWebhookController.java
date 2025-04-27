package com.savorybites.backend.api.webhook;

import com.savorybites.backend.model.OrderStatus;
import com.savorybites.backend.model.Orders;
import com.savorybites.backend.model.TrackingData;
import com.savorybites.backend.repository.OrdersRepository;
import com.savorybites.backend.repository.TrackingDataRepository;
import com.savorybites.backend.service.EmailService;
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

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/stripe")
public class StripeWebhookController {

    private final OrdersRepository ordersRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final TrackingDataRepository trackingDataRepository;
    private final EmailService emailService;

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

        switch (event.getType()) {
            case "payment_intent.succeeded":
                 handleSuccessfulPayment((PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow());
            case "payment_intent.payment_failed":
                handleFailedPayment((PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow());
            case "charge.succeeded":
                handleSuccessfulCharge((Charge) event.getDataObjectDeserializer().getObject().orElseThrow());
            case "application_fee.created":
                handleApplicationFeeCreated((ApplicationFee) event.getDataObjectDeserializer().getObject().orElseThrow());
            case "transfer.created":
                handleTransferCreated((Transfer) event.getDataObjectDeserializer().getObject().orElseThrow());
            case "charge.canceled":
                //handleCanceledPayment((ApplicationFee) event.getDataObjectDeserializer().getObject().orElseThrow());
            default:
                log.info("Unhandled event type: {}", event.getType());
                return ResponseEntity.ok("Unhandled event type");
        }
    }

    private void notifyUserOrderUpdate(Orders orders) {
        simpMessagingTemplate.convertAndSend(
                "/topic/user/" + orders.getUser().getId() + "/ordersUpdate",
                "Your Order (ID: " + orders.getId() + ") got a status update (" + orders.getStatus() + ")"
        );
    }

    public void handleSuccessfulPayment(PaymentIntent paymentIntent) {
        log.info("Handling successful payment for PaymentIntent: {}", paymentIntent.getId());

        String orderId = paymentIntent.getMetadata().get("order_id");
        if (orderId == null) {
            log.error("No order_id in PaymentIntent metadata");
            return;
        }
        long orderID=Long.parseLong(orderId);

        Orders order = ordersRepository.findById(orderID).orElse(null);
        if (order == null) {
            log.error("Orders not found for ID: {}", orderId);
            return;
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setPaid(true);

        order.setPaymentIntentId(paymentIntent.getId());
        ordersRepository.save(order);

        TrackingData trackingData=trackingDataRepository.findByOrder(order).orElseThrow();
        trackingData.setCompletedAt(LocalDateTime.now());
        trackingDataRepository.save(trackingData);

        notifyUserOrderUpdate(order);
    }

    public void handleFailedPayment(PaymentIntent paymentIntent) {

        log.info("Handling failed payment for PaymentIntent: {}", paymentIntent.getId());

        String orderId = paymentIntent.getMetadata().get("order_id");
        if (orderId == null) {
            log.error("No order_id in PaymentIntent metadata");
            return;
        }
        long orderID=Long.parseLong(orderId);

        Orders orders = ordersRepository.findById(orderID).orElse(null);
        if (orders == null) {
            log.error("Orders not found for ID: {}", orderId);
            return;
        }

        orders.setStatus(OrderStatus.PAYMENT_FAILED);
        ordersRepository.save(orders);

        notifyUserOrderUpdate(orders);
    }

    public void handleCanceledPayment(PaymentIntent paymentIntent) {
        log.info("Handling canceled payment for PaymentIntent: {}", paymentIntent.getId());

        String orderId = paymentIntent.getMetadata().get("order_id");
        if (orderId == null) {
            log.error("No order_id in PaymentIntent metadata");
            return;
        }
        long orderID=Long.parseLong(orderId);

        Orders orders = ordersRepository.findById(orderID).orElse(null);
        if (orders == null) {
            log.error("Orders not found for ID: {}", orderId);
            return;
        }

        orders.setStatus(OrderStatus.CANCELLED);
        ordersRepository.save(orders);

        notifyUserOrderUpdate(orders);
        emailService.sendOrderCancelledByUserEmail(orders.getUser().getUsername(),orders.getUser().getEmail(), orders.getId());
    }

    public void handleSuccessfulCharge(Charge charge) {
        log.info("Handling successful charge: {}", charge.getId());

        String orderId = charge.getMetadata().get("order_id");
        if (orderId == null) {
            log.warn("No order_id in Charge metadata");
            return;
        }
        long orderID=Long.parseLong(orderId);

        Orders orders = ordersRepository.findById(orderID).orElse(null);
        if (orders != null && orders.getChargeId() == null) {
            orders.setChargeId(charge.getId());
            ordersRepository.save(orders);
        }
    }

    public void handleTransferCreated(Transfer transfer) {
        log.info("Handling transfer created: {}", transfer.getId());

        String orderId = transfer.getMetadata().get("order_id");
        if (orderId == null) {
            log.warn("No order_id in Transfer metadata");
            return;
        }


        long orderID = Long.parseLong(orderId);
        Orders orders = ordersRepository.findById(orderID).orElseThrow();
        simpMessagingTemplate.convertAndSend(
                "/topic/restaurant/" + orders.getRestaurant().getId() + "/ordersUpdate",
                "Your have received the payment for the order (ID : "+orders.getId()+")");
    }

    public void handleApplicationFeeCreated(ApplicationFee applicationFee) {
        log.info("Handling application fee created: {}", applicationFee.getId());

        // Retrieve the charge ID from the application fee
        String chargeId = applicationFee.getCharge();

        // Find the associated order using the charge ID (assuming charge ID was saved in the order)
        Orders order = ordersRepository.findByChargeId(chargeId)
                .orElseThrow(() -> new RuntimeException("Order not found for chargeId: " + chargeId));


        // Send a notification to the admin or your internal system about the fee (optional)
        simpMessagingTemplate.convertAndSend(
                "/topic/admin/applicationFeeUpdate",
                "Application fee of" + applicationFee.getAmount()/100 + " DH was charged for Order ID: " + order.getId()
        );
    }
}