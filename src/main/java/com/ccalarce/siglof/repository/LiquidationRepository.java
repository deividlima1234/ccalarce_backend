package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Liquidation;
import com.ccalarce.siglof.model.enums.LiquidationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiquidationRepository extends JpaRepository<Liquidation, Long> {
    List<Liquidation> findByStatus(LiquidationStatus status);

    @Query("SELECT l FROM Liquidation l WHERE " +
            "(:driverId IS NULL OR l.route.driver.id = :driverId) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:startDate IS NULL OR l.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR l.createdAt <= :endDate)")
    Page<Liquidation> findHistory(
            @Param("driverId") Long driverId,
            @Param("status") LiquidationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
