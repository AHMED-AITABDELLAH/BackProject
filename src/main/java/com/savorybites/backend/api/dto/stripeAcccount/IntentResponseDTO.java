package com.savorybites.backend.api.dto.stripeAcccount;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class IntentResponseDTO {
    private long platformPaymentId;
    private String clientSecret;
}
