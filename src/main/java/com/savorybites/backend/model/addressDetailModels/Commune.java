package com.savorybites.backend.model.addressDetailModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "commune")
public class Commune {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "commune_name", nullable = false)
    private String communeName;

    @ManyToOne
    private Province province;

    @Column(name="commune_type", nullable = false)
    private String communeType;

}