package com.savorybites.backend.model;

import com.savorybites.backend.api.dto.address.AddressShortDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TrackingData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime completedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime preparationStartedAt;

    private LocalDateTime pickedUpAt;

    private LocalDateTime deliveredAt;

    @ManyToOne
    private Courier courier;

    @OneToOne
    private Orders order;
}
