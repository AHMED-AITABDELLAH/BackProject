package com.savorybites.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "local_user")
public class LocalUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false,unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name="fname")
    private String firstName;

    @Column(name="lname")
    private String lastName;

    @Column(name = "username")
    private String username;

    @Column(name="phone")
    private String phone;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();


    @Column(name="profile_img")
    private String profileImg;

    @Column(name="email",nullable = false,unique = true)
    @Email
    private String email;


    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;


    @Column(nullable = false)
    private Boolean isVerified = false;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;



    @JsonIgnore
    @OrderBy("id desc")
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ClientPaymentMethod> clientPaymentMethods;



    @PrePersist
    protected void onCreate() {
        if(isVerified == null)
            isVerified = false;
    }
}
