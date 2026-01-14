package com.ccalarce.siglof.service;

import com.ccalarce.siglof.dto.SaleRequest;
import com.ccalarce.siglof.model.entity.*;
import com.ccalarce.siglof.model.enums.RouteStatus;
import com.ccalarce.siglof.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SaleService {

        private final SaleRepository saleRepository;
        private final RouteRepository routeRepository;
        private final ClientRepository clientRepository;
        private final ProductRepository productRepository;

        @Transactional
        public Sale registerSale(SaleRequest request) {
                Route route = routeRepository.findById(request.getRouteId())
                                .orElseThrow(() -> new RuntimeException("Route not found"));

                if (route.getStatus() != RouteStatus.OPEN) {
                        throw new RuntimeException("Route is not OPEN");
                }

                // Validate Security (Repartidor can only sell on their route)
                User currentUser = (User) org.springframework.security.core.context.SecurityContextHolder.getContext()
                                .getAuthentication().getPrincipal();
                if (currentUser.getRole() == com.ccalarce.siglof.model.enums.Role.REPARTIDOR
                                && !route.getDriver().getId().equals(currentUser.getId())) {
                        throw new RuntimeException("Access Denied: You are not the driver of this route");
                }

                Client client = clientRepository.findById(request.getClientId())
                                .orElseThrow(() -> new RuntimeException("Client not found"));

                Sale sale = Sale.builder()
                                .route(route)
                                .client(client)
                                .paymentMethod(request.getPaymentMethod())
                                .latitude(request.getLatitude())
                                .longitude(request.getLongitude())
                                .details(new ArrayList<>())
                                .build();

                BigDecimal totalAmount = BigDecimal.ZERO;

                for (SaleRequest.SaleItem item : request.getItems()) {
                        Product product = productRepository.findById(item.getProductId())
                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                        // Descontar Stock de la RUTA (RouteStock)
                        RouteStock routeStock = route.getStock().stream()
                                        .filter(rs -> rs.getProduct().getId().equals(item.getProductId()))
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("Product not loaded in this route"));

                        if (routeStock.getCurrentQuantity() < item.getQuantity()) {
                                throw new RuntimeException(
                                                "Insufficient stock in route for product: " + product.getName());
                        }
                        routeStock.setCurrentQuantity(routeStock.getCurrentQuantity() - item.getQuantity());

                        // Calcular Subtotal
                        BigDecimal unitPrice = product.getPrice(); // Assumed fixed price for now
                        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                        totalAmount = totalAmount.add(subtotal);

                        // Crear Detalle
                        SaleDetail detail = SaleDetail.builder()
                                        .sale(sale)
                                        .product(product)
                                        .quantity(item.getQuantity())
                                        .unitPrice(unitPrice)
                                        .subtotal(subtotal)
                                        .build();

                        sale.getDetails().add(detail);
                }

                sale.setTotalAmount(totalAmount);

                // Save Route to persist stock changes (Cascade)
                routeRepository.save(route);
                // Save Sale (Cascade to details)
                return saleRepository.save(sale);
        }
}
