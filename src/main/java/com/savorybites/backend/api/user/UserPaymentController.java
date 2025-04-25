package com.savorybites.backend.api.user;

import com.savorybites.backend.api.dto.user.PaymentMethodDTO;
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
@RequestMapping("/user/paymentMethods")
public class UserPaymentController {


    private final StripeService stripeService;

    /**
     * Create SetupIntent and return the client secret to frontend.
     * The frontend will use this to collect and save a new card.
     */
    @PostMapping("/setupIntent")
    public ResponseEntity<?> createSetupIntent(@AuthenticationPrincipal LocalUser user) throws StripeException {
        String clientSecret = stripeService.createSetupIntent(user);
        System.out.println("niya tsaftat : "+clientSecret);
        return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
    }

    /**
     * After the frontend collects the card via Stripe Elements,
     * it will call this API with the `paymentMethodId` returned by Stripe.
     */
    @PostMapping("/savePaymentMethod")
    public ResponseEntity<?> savePaymentMethod(
            @AuthenticationPrincipal LocalUser user,
            @RequestBody Map<String, String> request
    ) throws StripeException {
        System.out.println("niya twslat bdina nsaviw : "+request.get("paymentMethodId"));

        String paymentMethodId = request.get("paymentMethodId");
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing paymentMethodId");
        }
        System.out.println("ngolo bsmlah");

        stripeService.savePaymentMethod(user, paymentMethodId);
        return ResponseEntity.ok("Card saved successfully");
    }

    /**
     * Get all saved cards (payment methods) for the logged-in user.
     */
    @GetMapping("/")
    public ResponseEntity<?> getUserPaymentMethods(@AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(stripeService.getPaymentMethods(user).stream().map(PaymentMethodDTO::new).toList());
    }

    /**
     * Mark a card as the default payment method.
     */
    @PutMapping("/{id}/default")
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaymentMethod(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long id
    ) throws StripeException {
        stripeService.deletePaymentMethod(id, user);
        return ResponseEntity.ok("Payment method deleted");
    }
}
