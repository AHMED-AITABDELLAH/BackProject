package com.savorybites.backend.service;

import com.savorybites.backend.model.*;
import com.savorybites.backend.repository.OrdersRepository;
import com.savorybites.backend.repository.PlatformPaymentMethodRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.TrackingDataRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Ref;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {


    private final SimpMessagingTemplate simpMessagingTemplate;
    private final OrdersRepository ordersRepository;
    private final TrackingDataRepository trackingDataRepository;
    private final EmailService emailService;
    @Value("${app.frontend.url}")
    private String frontUrl;
    private final PlatformPaymentMethodRepository platformPaymentMethodRepository;

    /**User Logic**/

    private String createOrGetStripeCustomer(LocalUser user) throws StripeException {
        // Check if any payment method exists for the user
        Optional<PlatformPaymentMethod> existing = platformPaymentMethodRepository
                .findFirstByUserOrderByIdAsc(user);

        if (existing.isPresent()) {
            System.out.println("We get the existing one");
            return existing.get().getStripeCustomerId();
        }

        System.out.println("We create new one :((((");
        Customer customer = Customer.create(CustomerCreateParams.builder()
                .setEmail(user.getEmail()).build());

        return customer.getId();
    }

    public String createSetupIntent(LocalUser user) throws StripeException {
        String customerId = createOrGetStripeCustomer(user);
        SetupIntent intent = SetupIntent.create(
                SetupIntentCreateParams.builder()
                        .setCustomer(customerId)
                        .addPaymentMethodType("card")
                        .build()
        );
        return intent.getClientSecret();
    }

    @Transactional
    public void savePaymentMethod(LocalUser user, String paymentMethodId) throws StripeException {
        System.out.println("1");
        String customerId = createOrGetStripeCustomer(user);
        System.out.println("2");

        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

        System.out.println("3");
        PlatformPaymentMethod clientPm = new PlatformPaymentMethod();
        clientPm.setUser(user);
        clientPm.setStripeCustomerId(customerId);
        clientPm.setStripePaymentMethodId(paymentMethod.getId());
        clientPm.setBrand(paymentMethod.getCard().getBrand());
        clientPm.setLast4(paymentMethod.getCard().getLast4());
        clientPm.setExpMonth(paymentMethod.getCard().getExpMonth());
        clientPm.setExpYear(paymentMethod.getCard().getExpYear());
        System.out.println("salina lhfla");


        boolean hasDefault = platformPaymentMethodRepository.existsByUserAndIsDefaultTrue(user);
        clientPm.setIsDefault(!hasDefault); // First card = default


        platformPaymentMethodRepository.save(clientPm);
        System.out.println("raho tma");

    }

    public List<PlatformPaymentMethod> getPaymentMethods(LocalUser user) {
        return platformPaymentMethodRepository.findAllByUser(user);
    }

    @Transactional
    public void setDefaultPaymentMethod(Long methodId, LocalUser user) {
        platformPaymentMethodRepository
                .findByUserAndIsDefaultTrue(user)
                .orElseThrow()
                .setIsDefault(false);

        PlatformPaymentMethod method = platformPaymentMethodRepository.findByIdAndUser(methodId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        method.setIsDefault(true);
        platformPaymentMethodRepository.save(method);
    }

    @Transactional
    public void deletePaymentMethod(Long methodId, LocalUser user) throws StripeException {
        PlatformPaymentMethod method = platformPaymentMethodRepository.findByIdAndUser(methodId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (method.getStripePaymentMethodId() != null) {
            PaymentMethod.retrieve(method.getStripePaymentMethodId()).detach();
        }

        platformPaymentMethodRepository.delete(method);
    }

    /**Restaurant Logic**/

    public String createAccount(String emailRestaurant) throws StripeException {

        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS) // Express account
                .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                .setCountry("FR") // Set the country (example: FR, GB, US, or MA for Morocco if needed)
                .setEmail(emailRestaurant) // Optional: pre-fill email if you want
                .build();

        // Actually create the account
        Account account = Account.create(params);
        return account.getId();
    }

    public String createAccountLink(String accountId) throws StripeException {
        AccountLink accountLink = AccountLink.create(
                AccountLinkCreateParams.builder()
                        .setAccount(accountId)
                        .setReturnUrl(frontUrl +"/restaurant/stripe/success")
                        .setRefreshUrl(frontUrl +"/restaurant/stripe/failed")
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build()
        );
        return accountLink.getUrl();
    }


    /**Webhook Handling**/

    private void notifyUserOrderUpdate(Orders orders) {
        simpMessagingTemplate.convertAndSend(
                "/topic/user/" + orders.getUser().getId() + "/ordersUpdate",
                "Your Order (ID: " + orders.getId() + ") got a status update (" + orders.getStatus() + ")"
        );
    }

    private void handleSuccessfulPayment(PaymentIntent paymentIntent) {
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

    private void handleFailedPayment(PaymentIntent paymentIntent) {

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

    private void handleCanceledPayment(PaymentIntent paymentIntent) {
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

    private void handleSuccessfulCharge(Charge charge) {
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

    private void handleTransferCreated(Transfer transfer) {
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

    private void handleApplicationFeeCreated(ApplicationFee applicationFee) {
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
