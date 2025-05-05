package com.savorybites.backend.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter@AllArgsConstructor
public class CartDetails {
    private double total;
    private double shipping;
}
