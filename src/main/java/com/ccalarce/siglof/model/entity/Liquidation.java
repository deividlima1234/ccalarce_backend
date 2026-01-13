package com.ccalarce.siglof.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "liquidations")
public class Liquidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private BigDecimal totalCash;

    @Column(nullable = false)
    private BigDecimal totalDigital;

    @Column(nullable = false)
    private Integer totalItemsSold;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Can add differences/discrepancies later

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
