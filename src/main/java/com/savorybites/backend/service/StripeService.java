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

}
