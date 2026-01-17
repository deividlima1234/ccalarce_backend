package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.dto.DashboardDTO;
import com.ccalarce.siglof.model.enums.RouteStatus;
import com.ccalarce.siglof.repository.ClientRepository;
import com.ccalarce.siglof.repository.ProductRepository;
import com.ccalarce.siglof.repository.RouteRepository;
import com.ccalarce.siglof.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final RouteRepository routeRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public DashboardDTO getSummary() {
        BigDecimal totalSales = saleRepository.sumTotalSalesByDate(LocalDate.now());
        Integer activeRoutes = routeRepository.countByStatus(RouteStatus.OPEN);
        Long totalClients = clientRepository.count();
        Integer lowStock = productRepository.countByStockLessThan(10);
        BigDecimal pendingBalance = saleRepository.sumPendingBalance();
        String topProduct = saleRepository.findTopSellingProductToday();

        // Calculate Routes Efficiency
        Double efficiency = 0.0;
        java.util.List<Object[]> stockSummary = routeRepository.getStockSummaryForActiveRoutes();
        if (stockSummary != null && !stockSummary.isEmpty() && stockSummary.get(0)[0] != null) {
            Long initialTotal = (Long) stockSummary.get(0)[0];
            Long currentTotal = (Long) stockSummary.get(0)[1];
            if (initialTotal > 0) {
                efficiency = ((initialTotal - currentTotal) * 100.0) / initialTotal;
            }
        }

        return DashboardDTO.builder()
                .totalSalesToday(totalSales)
                .activeRoutesCount(activeRoutes)
                .totalClients(totalClients)
                .lowStockAlerts(lowStock)
                .pendingBalance(pendingBalance)
                .topSellingProduct(topProduct != null ? topProduct : "N/A")
                .routesCompletion(Math.round(efficiency * 100.0) / 100.0) // Round to 2 decimals
                .build();
    }
}
