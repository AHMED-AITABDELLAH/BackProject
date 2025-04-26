package com.savorybites.backend.service;

import com.savorybites.backend.model.PlatformPaymentMethod;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.PlatformPaymentMethodRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeService {


    @Value("${app.frontend.url}")
    private String frontUrl;
    private final PlatformPaymentMethodRepository platformPaymentMethodRepository;
    private final RestaurantRepository restaurantRepository;

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

    /*

    @Transactional
    public String createExpressAccountAndOnboardingLink(Restaurant restaurant) throws StripeException {

        // 1. Create Stripe test Account
        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.CUSTOM)
                .setCountry("GB") // Use the country code of the restaurant (UK)
                .setEmail(restaurant.getEmail())
                .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                // Add required capabilities
                .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                        .setRequested(true)
                                        .build())
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                        .setRequested(true)
                                        .build())
                                .build()
                )
                .build();

        Account account = Account.create(params);

        // 2. Save Stripe account ID to restaurant entity
        restaurant.setStripeAccountId(account.getId());
        restaurantRepository.save(restaurant);

        // 3. Create Onboarding link
        AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                .setAccount(account.getId())
                .setRefreshUrl(refreshUrl) // redirect here if onboarding fails
                .setReturnUrl(returnUrl)   // redirect here after success
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink link = AccountLink.create(linkParams);

        return link.getUrl(); // Send this to the frontend to redirect the restaurant
    }

*/


    public String createAccount() throws StripeException {
        Account account = Account.create(AccountCreateParams.builder().build());
        return account.getId();
    }

    public String createAccountLink(String accountId) throws StripeException {
        AccountLink accountLink = AccountLink.create(
                AccountLinkCreateParams.builder()
                        .setAccount(accountId)
                        .setReturnUrl(frontUrl +"/restaurant/stripe/" + accountId +"/return")
                        .setRefreshUrl(frontUrl +"/auth/restaurant/stripe/" + accountId +"/refresh")
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build()
        );
        return accountLink.getUrl();
    }


}
