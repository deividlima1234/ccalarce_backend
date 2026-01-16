package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.model.entity.TokenQR;
import com.ccalarce.siglof.service.TokenQRService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
public class TokenQRController {

    private final TokenQRService service;

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<TokenQR>> generateBatch(@RequestBody BatchRequest request) {
        return ResponseEntity.ok(service.generateBatch(request.getQuantity()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<TokenQR>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'REPARTIDOR')")
    public ResponseEntity<TokenQR> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }

    @Data
    public static class BatchRequest {
        private int quantity;
    }
}
