package com.savorybites.backend.api.dto.user;

import com.savorybites.backend.model.PlatformPaymentMethod;
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

    public PaymentMethodDTO(PlatformPaymentMethod platformPaymentMethod){
        this.id= platformPaymentMethod.getId();
        this.brand= platformPaymentMethod.getBrand();
        this.expMonth= platformPaymentMethod.getExpMonth();
        this.expYear= platformPaymentMethod.getExpYear();
        this.last4= platformPaymentMethod.getLast4();
        this.isDefault= platformPaymentMethod.getIsDefault();
    }

}


