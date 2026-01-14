package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Sale;
import com.ccalarce.siglof.model.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByRouteId(Long routeId);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.route.id = :routeId AND s.paymentMethod = :method")
    BigDecimal sumTotalByRouteAndMethod(@Param("routeId") Long routeId, @Param("method") PaymentMethod method);

    @Query("SELECT COALESCE(SUM(sd.quantity), 0) FROM SaleDetail sd JOIN sd.sale s WHERE s.route.id = :routeId")
    Integer sumTotalItemsByRoute(@Param("routeId") Long routeId);
}
