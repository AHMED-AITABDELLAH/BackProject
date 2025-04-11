package com.savorybites.backend.model.addressDetailModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "province")
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "province_name", nullable = false)
    private String provinceName;

    @ManyToOne
    private Region region;

}