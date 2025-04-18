package com.savorybites.backend.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter


public class WeeklyOrderStats {

    private String weekLabel;      // e.g. "2025-W15"
    private int declinedOrdersCount;
    private int deliveredOrdersCount;
    private int CancelledOrdersCount;
}