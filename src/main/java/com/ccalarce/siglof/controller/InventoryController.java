package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.model.entity.InventoryMovement;
import com.ccalarce.siglof.model.enums.InventoryMovementType;
import com.ccalarce.siglof.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/movement")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<InventoryMovement> createMovement(@RequestBody MovementRequest request) {
        return ResponseEntity.ok(
                service.registerMovement(
                        request.getProductId(),
                        request.getQuantity(),
                        request.getType(),
                        request.getReason()));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<InventoryMovement>> getAllMovements() {
        return ResponseEntity.ok(service.getAllMovements());
    }

    @Data
    public static class MovementRequest {
        private Long productId;
        private Integer quantity;
        private InventoryMovementType type;
        private String reason;
    }
}
