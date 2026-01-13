package com.ccalarce.siglof.dto;

import lombok.Data;
import java.util.List;

@Data
public class CloseRouteRequest {
    private Long routeId;
    private List<ReturnedItem> savedStock; // Stock actual físico que vuelve

    @Data
    public static class ReturnedItem {
        private Long productId;
        private Integer quantity;
    }
}
