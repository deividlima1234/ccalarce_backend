package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.dto.SaleRequest;
import com.ccalarce.siglof.model.entity.Sale;
import com.ccalarce.siglof.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'REPARTIDOR')")
    public ResponseEntity<Sale> registerSale(@RequestBody SaleRequest request) {
        return ResponseEntity.ok(service.registerSale(request));
    }

    /**
     * Get all sales for a specific route (Admin/SuperAdmin dashboard)
     */
    @GetMapping("/route/{routeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<Sale>> getSalesByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.findByRouteId(routeId));
    }

    /**
     * Get sales history for the current driver's active route (Repartidor mobile)
     */
    @GetMapping("/my-sales")
    @PreAuthorize("hasRole('REPARTIDOR')")
    public ResponseEntity<List<Sale>> getMySales() {
        return ResponseEntity.ok(service.findByCurrentDriver());
    }
}
