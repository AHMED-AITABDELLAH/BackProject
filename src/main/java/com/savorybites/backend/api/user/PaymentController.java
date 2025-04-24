package com.savorybites.backend.api.user;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/paymentMethods")
public class PaymentController {


    private final StripeService stripeService;

    /**
     * Create SetupIntent and return the client secret to frontend.
     * The frontend will use this to collect and save a new card.
     */
    @PostMapping("/setup-intent")
    public ResponseEntity<?> createSetupIntent(@AuthenticationPrincipal LocalUser user) throws StripeException {
        String clientSecret = stripeService.createSetupIntent(user);
        return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
    }

    /**
     * After the frontend collects the card via Stripe Elements,
     * it will call this API with the `paymentMethodId` returned by Stripe.
     */
    @PostMapping("/payment-methods")
    public ResponseEntity<?> savePaymentMethod(
            @AuthenticationPrincipal LocalUser user,
            @RequestBody Map<String, String> request
    ) throws StripeException {
        String paymentMethodId = request.get("paymentMethodId");
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing paymentMethodId");
        }
        stripeService.savePaymentMethod(user, paymentMethodId);
        return ResponseEntity.ok("Card saved successfully");
    }

    /**
     * Get all saved cards (payment methods) for the logged-in user.
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getUserPaymentMethods(@AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(stripeService.getPaymentMethods(user));
    }

    /**
     * Mark a card as the default payment method.
     */
    @PutMapping("/payment-methods/{id}/default")
    public ResponseEntity<?> setDefaultMethod(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long id
    ) {
        stripeService.setDefaultPaymentMethod(id, user);
        return ResponseEntity.ok("Default payment method updated");
    }

    /**
     * Delete a saved payment method.
     */
    @DeleteMapping("/payment-methods/{id}")
    public ResponseEntity<?> deletePaymentMethod(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long id
    ) throws StripeException {
        stripeService.deletePaymentMethod(id, user);
        return ResponseEntity.ok("Payment method deleted");
    }
}
