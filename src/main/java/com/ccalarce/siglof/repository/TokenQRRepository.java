package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.TokenQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenQRRepository extends JpaRepository<TokenQR, Long> {
    Optional<TokenQR> findByCode(String code);

    /**
     * Optimized query to load TokenQR with Client in a single query.
     * Use this when you need to access the client relationship.
     */
    @Query("SELECT t FROM TokenQR t LEFT JOIN FETCH t.client WHERE t.code = :code")
    Optional<TokenQR> findByCodeWithClient(@Param("code") String code);
}
