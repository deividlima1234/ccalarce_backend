package com.ccalarce.siglof.service;

import com.ccalarce.siglof.dto.OpenRouteRequest;
import com.ccalarce.siglof.model.entity.*;
import com.ccalarce.siglof.model.enums.InventoryMovementType;
import com.ccalarce.siglof.model.enums.RouteStatus;
import com.ccalarce.siglof.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

        private final RouteRepository routeRepository;
        private final VehicleRepository vehicleRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final InventoryService inventoryService;

        @Transactional
        @com.ccalarce.siglof.annotation.Auditable(action = "OPEN_ROUTE")
        public Route openRoute(OpenRouteRequest request) {
                Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
                User driver = userRepository.findById(request.getDriverId())
                                .orElseThrow(() -> new RuntimeException("Driver not found"));

                Route route = Route.builder()
                                .vehicle(vehicle)
                                .driver(driver)
                                .status(RouteStatus.OPEN)
                                .openedAt(LocalDateTime.now())
                                .stock(new ArrayList<>())
                                .build();

                // Process stock load
                for (OpenRouteRequest.StockItem item : request.getStock()) {
                        // Deduct from Plant (InventoryService handles logic and movement record)
                        inventoryService.registerMovement(
                                        item.getProductId(),
                                        item.getQuantity(),
                                        InventoryMovementType.ROUTE_LOAD,
                                        "Load for Vehicle " + vehicle.getPlate());

                        // Add to Route Stock
                        Product product = productRepository.findById(item.getProductId()).get();
                        RouteStock routeStock = RouteStock.builder()
                                        .route(route)
                                        .product(product)
                                        .initialQuantity(item.getQuantity())
                                        .currentQuantity(item.getQuantity())
                                        .build();

                        route.getStock().add(routeStock);
                }

                return routeRepository.save(route);
        }

        public List<Route> findActiveRoutes() {
                return routeRepository.findByStatus(RouteStatus.OPEN);
        }
}
