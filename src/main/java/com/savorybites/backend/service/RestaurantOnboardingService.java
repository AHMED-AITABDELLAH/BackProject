package com.savorybites.backend.service;


import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.RestaurantRepository;
import com.stripe.exception.StripeException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class RestaurantOnboardingService {




    private final StripeService stripeAccountService;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public ResponseEntity<String> onboardRestaurant(Restaurant restaurant) throws StripeException {

        Restaurant managedRestaurant=restaurantRepository.findById(restaurant.getId()).orElseThrow(
                ()-> new EntityNotFoundException("No such restaurant.")
        );
        // 1. Create a Stripe account Or get(outside transaction)
        String stripeAccountId = restaurant.getStripeAccountId();

        if(stripeAccountId==null){
            stripeAccountId = stripeAccountService.createAccount(restaurant.getEmail());
            // 2. Update DB (transactional)
            restaurant.setStripeAccountId(stripeAccountId);
            restaurantRepository.save(restaurant);
        }

        // 3. Generate an onboarding link (outside transaction)
        return ResponseEntity.ok(stripeAccountService.createAccountLink(stripeAccountId));
    }
}
