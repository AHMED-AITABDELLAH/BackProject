package com.savorybites.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "local_user")
public class LocalUser {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false,unique = true)
    private UUID id;

    @Column(name="fname")
    private String firstName;

    @Column(name="lname")
    private String lastName;

    @Column(name="phone")
    private String phone;

    @Column(name="address")
    private String address;

    @Column(name="profile_img")
    private String profileImg;

    @Column(name="email",nullable = false,unique = true)
    @Email
    private String email;


    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;


}