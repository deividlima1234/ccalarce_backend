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

    @GetMapping("/history")
    // Relaxed security to debug: if authenticated, let them in. Service layer
    // handles logic.
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.data.domain.Page<Liquidation>> getHistory(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) com.ccalarce.siglof.model.enums.LiquidationStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            org.springframework.data.domain.Pageable pageable) {

        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;

        if (startDate != null && !startDate.trim().isEmpty()) {
            start = java.time.LocalDateTime.parse(startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            end = java.time.LocalDateTime.parse(endDate);
        }

        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        com.ccalarce.siglof.model.entity.User user = (com.ccalarce.siglof.model.entity.User) principal;

        System.out.println("DEBUG: User " + user.getUsername() + " Authorities: " + user.getAuthorities());

        try {
            return ResponseEntity.ok(service.getHistory(user, driverId, status, start, end, pageable));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error inside controller: " + e.getMessage());
        }
    }

    @GetMapping("/debug-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getDebugInfo() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String info = "Version: 1.0.1-FixDate\n";
        if (principal instanceof com.ccalarce.siglof.model.entity.User) {
            com.ccalarce.siglof.model.entity.User user = (com.ccalarce.siglof.model.entity.User) principal;
            info += "User: " + user.getUsername() + "\n";
            info += "Authorities: " + user.getAuthorities() + "\n";
        } else {
            info += "Principal: " + principal.toString();
        }
        return ResponseEntity.ok(info);
    }
}
