package com.ccalarce.siglof.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private BigDecimal totalSalesToday;
    private Integer activeRoutesCount;
    private Long totalClients;
    private Integer lowStockAlerts;
    private BigDecimal pendingBalance;
    private String topSellingProduct;
    private Double routesCompletion;
}
