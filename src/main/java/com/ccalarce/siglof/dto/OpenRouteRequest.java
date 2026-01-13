package com.ccalarce.siglof.dto;

import lombok.Data;
import java.util.List;

@Data
public class OpenRouteRequest {
    private Long vehicleId;
    private Long driverId;
    private List<StockItem> stock;

    @Data
    public static class StockItem {
        private Long productId;
        private Integer quantity;
    }
}
