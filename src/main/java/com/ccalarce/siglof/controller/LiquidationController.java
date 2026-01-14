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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'REPARTIDOR')")
    public ResponseEntity<Liquidation> closeRoute(@RequestBody CloseRouteRequest request) {
        return ResponseEntity.ok(service.closeRoute(request));
    }
}
