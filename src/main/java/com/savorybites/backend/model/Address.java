package com.savorybites.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Long id;

    @ManyToOne
    private LocalUser user;

    @OneToOne
    private Restaurant restaurant;

    @Column(name = "id", nullable = false)
    private String province;

    @Column(name = "street", nullable = false)
    private String street;


    @Column(name = "commune", nullable = false)
    private String commune;

    @Column(name = "region", nullable = false)
    private String region;



    @Column(name = "is_Default")
    private boolean isDefault;

}