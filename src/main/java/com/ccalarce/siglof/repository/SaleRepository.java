package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByRouteId(Long routeId);
}
