package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.dto.OpenRouteRequest;
import com.ccalarce.siglof.model.entity.Route;
import com.ccalarce.siglof.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService service;

    @PostMapping("/open")
    public ResponseEntity<Route> openRoute(@RequestBody OpenRouteRequest request) {
        return ResponseEntity.ok(service.openRoute(request));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Route>> getActiveRoutes() {
        return ResponseEntity.ok(service.findActiveRoutes());
    }
}
