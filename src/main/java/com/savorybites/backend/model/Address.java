package com.savorybites.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.savorybites.backend.model.addressDetailModels.Commune;
import com.savorybites.backend.model.addressDetailModels.Province;
import com.savorybites.backend.model.addressDetailModels.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
    //this is the address entity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private LocalUser user;

    @JsonIgnore
    @OneToOne(mappedBy = "address",orphanRemoval = true)
    private Restaurant restaurant;

    @Column(name = "title")
    private String title;

    @Column(name = "street", nullable = false)
    private String street;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @ManyToOne
    @JoinColumn(name = "commune_id")
    private Commune commune;


    @Column(name="latitude")
    private Double latitude;

    @Column(name="longitude")
    private Double longitude;

    @Column(name = "is_Default")
    private Boolean isDefault;

    @Column(name = "is_Coordination_Activated")
    private boolean isCoordinationActivated;


}