package com.savorybites.backend.model.verificationModels;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name="expiry_date",nullable = false)
    private Instant expiryDate;

    @ManyToOne
    @JoinColumn(name = "local_user_id")
    private LocalUser user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

}