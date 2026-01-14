package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.LoyaltyMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyMovementRepository extends JpaRepository<LoyaltyMovement, Long> {
}
