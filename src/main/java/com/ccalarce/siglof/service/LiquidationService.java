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
    @com.ccalarce.siglof.annotation.Auditable(action = "CLOSE_ROUTE")
    public Liquidation closeRoute(CloseRouteRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (route.getStatus() == RouteStatus.CLOSED) {
            throw new RuntimeException("Route is already CLOSED");
        }

        // 1. Calculate Sales Totals (Optimized with DB aggregation)
        BigDecimal totalCash = saleRepository.sumTotalByRouteAndMethod(route.getId(), PaymentMethod.CASH);
        if (totalCash == null)
            totalCash = BigDecimal.ZERO;

        // Sum other methods (YAPE, PLIN, CREDIT) efficiently if needed, or just sum
        // non-CASH
        // For simplicity and speed, let's assume strict separation.
        // If we want total Digital, we could do a simpler query or sum locally if few.
        // Actually, let's just sum all Non-Cash.
        // But to follow the repo pattern:
        BigDecimal totalYape = saleRepository.sumTotalByRouteAndMethod(route.getId(), PaymentMethod.YAPE);
        BigDecimal totalPlin = saleRepository.sumTotalByRouteAndMethod(route.getId(), PaymentMethod.PLIN);
        BigDecimal totalCredit = saleRepository.sumTotalByRouteAndMethod(route.getId(), PaymentMethod.CREDIT);

        BigDecimal totalDigital = (totalYape != null ? totalYape : BigDecimal.ZERO)
                .add(totalPlin != null ? totalPlin : BigDecimal.ZERO)
                .add(totalCredit != null ? totalCredit : BigDecimal.ZERO);

        Integer itemsSold = saleRepository.sumTotalItemsByRoute(route.getId());

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

    @Transactional
    @com.ccalarce.siglof.annotation.Auditable(action = "APPROVE_LIQUIDATION")
    public Liquidation approveLiquidation(Long id, User adminUser) {
        Liquidation liquidation = liquidationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Liquidation not found"));

        if (liquidation.getStatus() == com.ccalarce.siglof.model.enums.LiquidationStatus.APPROVED) {
            throw new RuntimeException("Liquidation is already APPROVED");
        }

        liquidation.setStatus(com.ccalarce.siglof.model.enums.LiquidationStatus.APPROVED);
        liquidation.setApprovedBy(adminUser);

        return liquidationRepository.save(liquidation);
    }

    /**
     * Get all liquidations with PENDING status (for Admin dashboard)
     */
    public List<Liquidation> findPendingLiquidations() {
        return liquidationRepository.findByStatus(com.ccalarce.siglof.model.enums.LiquidationStatus.PENDING);
    }
}
