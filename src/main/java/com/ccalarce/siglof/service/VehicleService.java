package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.entity.Vehicle;
import com.ccalarce.siglof.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository repository;

    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    public Vehicle findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    public Vehicle save(Vehicle vehicle) {
        return repository.save(vehicle);
    }

    public Vehicle update(Long id, Vehicle request) {
        Vehicle existing = findById(id);
        existing.setPlate(request.getPlate());
        existing.setBrand(request.getBrand());
        existing.setModel(request.getModel());
        existing.setCapacity(request.getCapacity());
        existing.setActive(request.getActive());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Vehicle existing = findById(id);
        existing.setActive(false);
        repository.save(existing);
    }
}
