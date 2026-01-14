package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.dto.OpenRouteRequest;
import com.ccalarce.siglof.model.entity.Route;
import com.ccalarce.siglof.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService service;

    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Route> openRoute(@RequestBody OpenRouteRequest request) {
        return ResponseEntity.ok(service.openRoute(request));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')") // Only admins see ALL active
    public ResponseEntity<List<Route>> getActiveRoutes() {
        return ResponseEntity.ok(service.findActiveRoutes());
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('REPARTIDOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Route> getCurrentRoute() {
        String username = (String) org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        // Note: JwtAuthenticationFilter sets authentication with username(String) or
        // UserDetails?
        // Let's verify JwtAuthenticationFilter logic. Usually principal is UserDetails
        // or String.
        // In SaleService we cast to User (entity) which means custom UserDetails
        // service implementation returns Entity.
        // BUT standard Spring Security returns UserDetails. Let's check safely.

        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String currentUsername;
        if (principal instanceof com.ccalarce.siglof.model.entity.User) {
            currentUsername = ((com.ccalarce.siglof.model.entity.User) principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }

        return ResponseEntity.ok(service.getCurrentRouteForDriver(currentUsername));
    }
}
