package com.savorybites.backend.api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ItemDetails {
    private String productName;
    private int quantity;
    private double total;
}
