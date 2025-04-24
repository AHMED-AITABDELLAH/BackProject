package com.savorybites.backend.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {

    private Long id;
    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;
    private Boolean isDefault;

}


