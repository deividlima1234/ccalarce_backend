package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.TokenQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenQRRepository extends JpaRepository<TokenQR, Long> {
    Optional<TokenQR> findByCode(String code);
}
