package com.savorybites.backend.api.dto.stripeAcccount;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter@AllArgsConstructor
public class PaymentSaveDTO {

    private long platformPaymentId;
    private String paymentMethodId;
}
