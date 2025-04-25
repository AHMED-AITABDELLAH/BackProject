package com.savorybites.backend.api.restaurant;

import com.savorybites.backend.api.dto.user.PaymentMethodDTO;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.service.RestaurantService;
import com.savorybites.backend.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/restaurant/paymentMethod")
public class RestaurantPaymentController {


    private final StripeService stripeService;
    private final RestaurantService restaurantService;

}
