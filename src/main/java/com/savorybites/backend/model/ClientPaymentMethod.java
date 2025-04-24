package com.savorybites.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "payment_method")
public class ClientPaymentMethod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_customer_id", nullable = false)
    private String stripeCustomerId;

    @Column(name = "stripe_payment_method_id")
    private String stripePaymentMethodId;

    private Boolean isDefault;

    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private LocalUser User;
}
