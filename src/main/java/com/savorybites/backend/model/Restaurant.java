package com.savorybites.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "restaurant")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id",unique = true, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name="title",nullable = false)
    private String title;

    @Column(name="description")
    private String description;

    @Column(name = "phone", unique = true ,nullable = false)
    private String phone;

    @Column(name = "email", unique = true,nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password", unique = true,nullable = false)
    private String password;

    @Column(name = "contact_email", unique = true,nullable = false)
    private String contactEmail;

    @Column(name = "paypal_email", unique = true,nullable = false)
    private String paypalEmail;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(name = "profile_banner")
    private String profileBanner;

    @JsonIgnore
    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING_APPROVAL;

    @JsonIgnore
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id", nullable = false)
    private Address address;


    @JsonIgnore
    @OneToMany(mappedBy = "restaurant",cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id desc")
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    @Column(name = "shipping_fees")
    private double shippingFees;

    @JsonIgnore
    @OneToMany(mappedBy = "restaurant", orphanRemoval = true,cascade = CascadeType.ALL)
    private List<Food> foods = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (isApproved == null) {
            isApproved = false;
        }
        if(isVerified == null)
            isVerified = false;
    }
}