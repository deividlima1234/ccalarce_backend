package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Liquidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiquidationRepository extends JpaRepository<Liquidation, Long> {
}
