package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Sale;
import com.ccalarce.siglof.model.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByRouteId(Long routeId);

    /**
     * Optimized query with JOIN FETCH to load Sales with all relationships.
     * Prevents N+1 queries by eagerly fetching route, client (with tokenQr), and
     * details (with product).
     */
    @Query("SELECT DISTINCT s FROM Sale s " +
            "LEFT JOIN FETCH s.route " +
            "LEFT JOIN FETCH s.client c " +
            "LEFT JOIN FETCH c.tokenQr " +
            "LEFT JOIN FETCH s.details d " +
            "LEFT JOIN FETCH d.product " +
            "WHERE s.route.id = :routeId")
    List<Sale> findByRouteIdWithDetails(@Param("routeId") Long routeId);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.route.id = :routeId AND s.paymentMethod = :method")
    BigDecimal sumTotalByRouteAndMethod(@Param("routeId") Long routeId, @Param("method") PaymentMethod method);

    @Query("SELECT COALESCE(SUM(sd.quantity), 0) FROM SaleDetail sd JOIN sd.sale s WHERE s.route.id = :routeId")
    Integer sumTotalItemsByRoute(@Param("routeId") Long routeId);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE DATE(s.createdAt) = :date")
    BigDecimal sumTotalSalesByDate(@Param("date") LocalDate date);
}
