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
        return DashboardDTO.builder()
                .totalSalesToday(saleRepository.sumTotalSalesByDate(LocalDate.now()))
                .activeRoutesCount(routeRepository.countByStatus(RouteStatus.EN_RUTA))
                .totalClients(clientRepository.count())
                .lowStockAlerts(productRepository.countByStockLessThan(10)) // Using 10 as default threshold
                .build();
    }
}
