package com.savorybites.backend.model.verificationModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "blacklisted_token")
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    @Column(name = "token",nullable = false,unique = true)
    private String token;

    @Column(name = "expiry_date",nullable = false,unique = true)
    private Instant expiryDate;


}