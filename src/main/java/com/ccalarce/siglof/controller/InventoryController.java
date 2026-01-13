package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.model.entity.InventoryMovement;
import com.ccalarce.siglof.model.enums.InventoryMovementType;
import com.ccalarce.siglof.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/movement")
    public ResponseEntity<InventoryMovement> createMovement(@RequestBody MovementRequest request) {
        return ResponseEntity.ok(
                service.registerMovement(
                        request.getProductId(),
                        request.getQuantity(),
                        request.getType(),
                        request.getReason()));
    }

    @Data
    public static class MovementRequest {
        private Long productId;
        private Integer quantity;
        private InventoryMovementType type;
        private String reason;
    }
}
