package com.savorybites.backend.service;


import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.RestaurantRepository;
import com.stripe.exception.StripeException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class RestaurantOnboardingService {




    private final StripeService stripeAccountService;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public String onboardRestaurant(Restaurant restaurant) throws StripeException {

        // 1. Create a Stripe account (outside transaction)
        String stripeAccountId = stripeAccountService.createAccount();

        // 2. Update DB (transactional)
        restaurant.setStripeAccountId(stripeAccountId);
        restaurantRepository.save(restaurant);

        // 3. Generate an onboarding link (outside transaction)
        return stripeAccountService.createAccountLink(stripeAccountId);
    }
}
