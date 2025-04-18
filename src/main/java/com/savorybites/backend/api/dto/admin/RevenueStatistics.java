package com.savorybites.backend.api.dto.admin;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
public class RevenueStatistics {


    private BigDecimal totalRevenue;
    private List<MonthlyRevenue> monthlyRevenue;
}
