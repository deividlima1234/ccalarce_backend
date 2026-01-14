package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.entity.InventoryMovement;
import com.ccalarce.siglof.model.entity.Product;
import com.ccalarce.siglof.model.enums.InventoryMovementType;
import com.ccalarce.siglof.repository.InventoryMovementRepository;
import com.ccalarce.siglof.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;

    @Transactional
    @com.ccalarce.siglof.annotation.Auditable(action = "REGISTER_MOVEMENT")
    public InventoryMovement registerMovement(Long productId, Integer quantity, InventoryMovementType type,
            String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update Product Stock
        if (type == InventoryMovementType.PURCHASE || type == InventoryMovementType.RETURN) {
            product.setStock(product.getStock() + quantity);
        } else if (type == InventoryMovementType.ROUTE_LOAD) {
            if (product.getStock() < quantity) {
                throw new RuntimeException("Insufficient stock in plant");
            }
            product.setStock(product.getStock() - quantity);
        } else if (type == InventoryMovementType.ADJUSTMENT) {
            // For adjustment, quantity can be positive (add) or negative (subtract)
            product.setStock(product.getStock() + quantity);
        }

        productRepository.save(product);

        // Record Movement
        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .type(type)
                .quantity(quantity)
                .reason(reason)
                .build();

        return movementRepository.save(movement);
    }
}
