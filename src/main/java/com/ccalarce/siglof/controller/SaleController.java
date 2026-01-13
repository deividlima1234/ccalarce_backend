package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.dto.SaleRequest;
import com.ccalarce.siglof.model.entity.Sale;
import com.ccalarce.siglof.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService service;

    @PostMapping
    public ResponseEntity<Sale> registerSale(@RequestBody SaleRequest request) {
        return ResponseEntity.ok(service.registerSale(request));
    }
}
