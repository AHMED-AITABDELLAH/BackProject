package com.savorybites.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "payment_method")
public class PlatformPaymentMethod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(name = "stripe_customer_id", nullable = false)
    private String stripeCustomerId;

    @JsonIgnore
    @Column(name = "stripe_payment_method_id", nullable = false)
    private String stripePaymentMethodId;

    private Boolean isDefault=false;

    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private LocalUser user;

}
