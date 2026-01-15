package com.ccalarce.siglof.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.ccalarce.siglof.model.enums.LiquidationStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiquidationStatus status;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * Nota/mensaje del Admin para el Repartidor
     * Ej: "Falta S/50 en efectivo, por favor revisar"
     */
    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = LiquidationStatus.PENDING;
        }
    }
}
