package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.dto.CloseRouteRequest;
import com.ccalarce.siglof.model.entity.Liquidation;
import com.ccalarce.siglof.service.LiquidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/liquidation")
@RequiredArgsConstructor
public class LiquidationController {

    private final LiquidationService service;

    @PostMapping("/close")
    @PreAuthorize("hasAnyRole('REPARTIDOR')") // Only Repartidor closes THEIR route (tunnel vision could be applied here
                                              // too theoretically, but routeId check is in service)
    public ResponseEntity<Liquidation> closeRoute(@RequestBody CloseRouteRequest request) {
        // Technically we should check if route belongs to user here too for total
        // security
        return ResponseEntity.ok(service.closeRoute(request));
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Liquidation> approveLiquidation(@PathVariable Long id) {
        // Get Admin User
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        com.ccalarce.siglof.model.entity.User adminUser = (com.ccalarce.siglof.model.entity.User) principal;

        return ResponseEntity.ok(service.approveLiquidation(id, adminUser));
    }
}
