package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.dto.CloseRouteRequest;
import com.ccalarce.siglof.dto.ReviewLiquidationRequest;
import com.ccalarce.siglof.model.entity.Liquidation;
import com.ccalarce.siglof.service.LiquidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/liquidation")
@RequiredArgsConstructor
public class LiquidationController {

    private final LiquidationService service;

    /**
     * List all pending liquidations (for Admin dashboard)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<Liquidation>> getPendingLiquidations() {
        return ResponseEntity.ok(service.findPendingLiquidations());
    }

    /**
     * List observed liquidations (for Driver notification)
     */
    @GetMapping("/observed")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'REPARTIDOR')")
    public ResponseEntity<List<Liquidation>> getObservedLiquidations() {
        return ResponseEntity.ok(service.findObservedLiquidations());
    }

    @PostMapping("/close")
    @PreAuthorize("hasAnyRole('REPARTIDOR')")
    public ResponseEntity<Liquidation> closeRoute(@RequestBody CloseRouteRequest request) {
        return ResponseEntity.ok(service.closeRoute(request));
    }

    /**
     * Review a liquidation (APPROVE, REJECT, or OBSERVE with note)
     */
    @PostMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Liquidation> reviewLiquidation(
            @PathVariable Long id,
            @RequestBody ReviewLiquidationRequest request) {

        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        com.ccalarce.siglof.model.entity.User adminUser = (com.ccalarce.siglof.model.entity.User) principal;

        return ResponseEntity.ok(service.reviewLiquidation(id, adminUser, request.getStatus(), request.getNote()));
    }
}
