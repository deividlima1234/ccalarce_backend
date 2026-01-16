package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.entity.Client;
import com.ccalarce.siglof.model.entity.TokenQR;
import com.ccalarce.siglof.model.enums.TokenStatus;
import com.ccalarce.siglof.repository.TokenQRRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenQRService {

    private final TokenQRRepository repository;

    @Transactional
    public List<TokenQR> generateBatch(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (quantity > 1000) {
            throw new IllegalArgumentException("Quantity cannot exceed 1000 per batch");
        }

        List<TokenQR> tokens = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            TokenQR token = TokenQR.builder()
                    .code(UUID.randomUUID().toString())
                    .status(TokenStatus.DISPONIBLE)
                    .build();
            tokens.add(token);
        }

        return repository.saveAll(tokens);
    }

    public TokenQR findByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("TokenQR not found: " + code));
    }

    @Transactional
    public TokenQR assignTokenToClient(String code, Client client) {
        TokenQR token = findByCode(code);

        if (token.getStatus() != TokenStatus.DISPONIBLE) {
            // If it's already assigned to THIS client, allow it (idempotency/correction)
            if (token.getStatus() == TokenStatus.ASIGNADO && token.getClient() != null
                    && token.getClient().getId().equals(client.getId())) {
                return token;
            }
            throw new RuntimeException("TokenQR is not available (Status: " + token.getStatus() + ")");
        }

        token.setStatus(TokenStatus.ASIGNADO);
        token.setAssignedAt(LocalDateTime.now());
        // NOTE: The relation is OneToOne mappedBy="tokenQr" in TokenQR.
        // The foreign key is in Client table. So we don't set client here directly
        // if TokenQR doesn't own the relationship.
        // However, looking at the entity code:
        // @OneToOne(mappedBy = "tokenQr") private Client client;
        // The Client entity owns the relationship: @JoinColumn(name = "token_qr_id")

        // So we just update the status here. The connection is made when Client is
        // saved.
        return repository.save(token);
    }

    public List<TokenQR> findAll() {
        return repository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }
}
