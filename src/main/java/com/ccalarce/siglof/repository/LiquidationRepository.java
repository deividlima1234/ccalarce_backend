package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Liquidation;
import com.ccalarce.siglof.model.enums.LiquidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiquidationRepository extends JpaRepository<Liquidation, Long>, JpaSpecificationExecutor<Liquidation> {
        List<Liquidation> findByStatus(LiquidationStatus status);
}
