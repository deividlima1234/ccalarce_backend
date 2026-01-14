package com.ccalarce.siglof.model.entity;

import com.ccalarce.siglof.model.enums.TokenStatus;
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
@Table(name = "tokens_qr")
public class TokenQR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // UUID representation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    @OneToOne(mappedBy = "tokenQr")
    private Client client;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TokenStatus.DISPONIBLE;
        }
    }
}
