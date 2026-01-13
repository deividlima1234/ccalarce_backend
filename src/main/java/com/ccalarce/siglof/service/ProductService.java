package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.entity.Product;
import com.ccalarce.siglof.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public List<Product> findAll() {
        return repository.findAll();
    }

    public List<Product> findAllActive() {
        return repository.findByActiveTrue();
    }

    public Product findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product save(Product product) {
        return repository.save(product);
    }

    public Product update(Long id, Product request) {
        Product existing = findById(id);
        existing.setName(request.getName());
        existing.setPrice(request.getPrice());
        existing.setActive(request.getActive());
        // Stock updates should ideally happen through inventory movements, not direct
        // edit, but allowing for now.
        if (request.getStock() != null) {
            existing.setStock(request.getStock());
        }
        return repository.save(existing);
    }

    public void delete(Long id) {
        // Soft delete preferred
        Product existing = findById(id);
        existing.setActive(false);
        repository.save(existing);
    }
}
