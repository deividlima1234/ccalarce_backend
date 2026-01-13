package com.ccalarce.siglof.service;

import com.ccalarce.siglof.dto.CloseRouteRequest;
import com.ccalarce.siglof.model.entity.*;
import com.ccalarce.siglof.model.enums.InventoryMovementType;
import com.ccalarce.siglof.model.enums.PaymentMethod;
import com.ccalarce.siglof.model.enums.RouteStatus;
import com.ccalarce.siglof.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidationService {

    private final RouteRepository routeRepository;
    private final SaleRepository saleRepository;
    private final LiquidationRepository liquidationRepository;
    private final InventoryService inventoryService;

    @Transactional
    public Liquidation closeRoute(CloseRouteRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (route.getStatus() == RouteStatus.CLOSED) {
            throw new RuntimeException("Route is already CLOSED");
        }

        // 1. Calculate Sales Totals
        List<Sale> sales = saleRepository.findByRouteId(route.getId());
        BigDecimal totalCash = BigDecimal.ZERO;
        BigDecimal totalDigital = BigDecimal.ZERO;
        int itemsSold = 0;

        for (Sale sale : sales) {
            if (sale.getPaymentMethod() == PaymentMethod.CASH) {
                totalCash = totalCash.add(sale.getTotalAmount());
            } else {
                totalDigital = totalDigital.add(sale.getTotalAmount());
            }
            itemsSold += sale.getDetails().stream().mapToInt(SaleDetail::getQuantity).sum();
        }

        // 2. Process Returns to Plant (Inventory)
        for (CloseRouteRequest.ReturnedItem item : request.getSavedStock()) {
            inventoryService.registerMovement(
                    item.getProductId(),
                    item.getQuantity(),
                    InventoryMovementType.RETURN,
                    "Return from Route " + route.getId());
        }

        // 3. Create Liquidation
        Liquidation liquidation = Liquidation.builder()
                .route(route)
                .totalCash(totalCash)
                .totalDigital(totalDigital)
                .totalItemsSold(itemsSold)
                .build();

        liquidationRepository.save(liquidation);

        // 4. Close Route
        route.setStatus(RouteStatus.CLOSED);
        route.setClosedAt(LocalDateTime.now());
        routeRepository.save(route);

        return liquidation;
    }
}
