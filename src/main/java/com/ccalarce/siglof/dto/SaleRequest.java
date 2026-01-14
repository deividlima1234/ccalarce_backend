package com.ccalarce.siglof.dto;

import com.ccalarce.siglof.model.enums.PaymentMethod;
import lombok.Data;
import java.util.List;

@Data
public class SaleRequest {
    private Long routeId;
    private Long clientId;
    private PaymentMethod paymentMethod;
    private Double latitude;
    private Double longitude;
    private List<SaleItem> items;

    @Data
    public static class SaleItem {
        private Long productId;
        private Integer quantity;
        // Price can be fetched from Product or sent from frontend (if variable).
        // For Phase 2, we fetch from Product.
    }
}
