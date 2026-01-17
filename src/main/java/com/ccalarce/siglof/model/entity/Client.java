package com.ccalarce.siglof.model.entity;

import com.ccalarce.siglof.model.enums.ClientType;
import com.ccalarce.siglof.model.enums.CommercialStatus;
import com.ccalarce.siglof.model.enums.PaymentFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String documentNumber; // DNI or RUC

    @Column(nullable = false)
    private String address;

    private String phoneNumber;

    private String zone;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private ClientType type;

    @Enumerated(EnumType.STRING)
    private CommercialStatus commercialStatus;

    @Enumerated(EnumType.STRING)
    private PaymentFrequency paymentFrequency;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "token_qr_id", referencedColumnName = "id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("client")
    private TokenQR tokenQr;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        if (this.commercialStatus == null) {
            this.commercialStatus = CommercialStatus.ACTIVO;
        }
    }
}
