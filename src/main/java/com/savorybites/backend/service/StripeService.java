package com.savorybites.backend.service;

import com.savorybites.backend.model.ClientPaymentMethod;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.repository.ClientPaymentMethodRepository;
import com.savorybites.backend.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SetupIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeService {

    private final ClientPaymentMethodRepository clientPaymentMethodRepository;


    public String createOrGetStripeCustomer(LocalUser user) throws StripeException {
        // Check if any payment method exists for the user
        Optional<ClientPaymentMethod> existing = clientPaymentMethodRepository
                .findFirstByUserOrderByIdAsc(user);

        if (existing.isPresent()) {
            return existing.get().getStripeCustomerId();
        }

        Customer customer = Customer.create(CustomerCreateParams.builder()
                .setEmail(user.getEmail()).build());

        // Save a blank payment method just to hold the customerId
        ClientPaymentMethod pm = new ClientPaymentMethod();
        pm.setUser(user);
        pm.setStripeCustomerId(customer.getId());
        pm.setIsDefault(false); // Placeholder, not a real card
        clientPaymentMethodRepository.save(pm);

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

    public void savePaymentMethod(LocalUser user, String paymentMethodId) throws StripeException {
        String customerId = createOrGetStripeCustomer(user);

        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        paymentMethod.attach(PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build());

        ClientPaymentMethod clientPm = new ClientPaymentMethod();
        clientPm.setUser(user);
        clientPm.setStripeCustomerId(customerId);
        clientPm.setStripePaymentMethodId(paymentMethod.getId());
        clientPm.setBrand(paymentMethod.getCard().getBrand());
        clientPm.setLast4(paymentMethod.getCard().getLast4());
        clientPm.setExpMonth(paymentMethod.getCard().getExpMonth());
        clientPm.setExpYear(paymentMethod.getCard().getExpYear());

        boolean hasDefault = clientPaymentMethodRepository.existsByUserAndIsDefaultTrue(user);
        clientPm.setIsDefault(!hasDefault); // First card = default

        clientPaymentMethodRepository.save(clientPm);
    }

    public List<ClientPaymentMethod> getPaymentMethods(LocalUser user) {
        return clientPaymentMethodRepository.findAllByUser(user);
    }

    public void setDefaultPaymentMethod(Long methodId, LocalUser user) {
        clientPaymentMethodRepository.clearAllDefaultsForUser(user.getId());

        ClientPaymentMethod method = clientPaymentMethodRepository.findByIdAndUser(methodId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        method.setIsDefault(true);
        clientPaymentMethodRepository.save(method);
    }

    public void deletePaymentMethod(Long methodId, LocalUser user) throws StripeException {
        ClientPaymentMethod method = clientPaymentMethodRepository.findByIdAndUser(methodId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (method.getStripePaymentMethodId() != null) {
            PaymentMethod.retrieve(method.getStripePaymentMethodId()).detach();
        }

        clientPaymentMethodRepository.delete(method);
    }
}
