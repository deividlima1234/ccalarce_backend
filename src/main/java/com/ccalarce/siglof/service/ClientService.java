package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.entity.Client;
import com.ccalarce.siglof.model.entity.User;
import com.ccalarce.siglof.model.enums.Role;
import com.ccalarce.siglof.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;
    private final TokenQRService tokenQRService;

    public List<Client> findAll() {
        return repository.findAllWithTokens();
    }

    public Client findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public Client save(Client client) {
        // Handle Token Assignment if provided
        if (client.getTokenQr() != null && client.getTokenQr().getCode() != null) {
            com.ccalarce.siglof.model.entity.TokenQR assignedToken = tokenQRService
                    .assignTokenToClient(client.getTokenQr().getCode(), client);
            client.setTokenQr(assignedToken);
        }
        return repository.save(client);
    }

    public Client update(Long id, Client clientRequest) {
        Client existing = findById(id);

        // Get current user to check role
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // If REPARTIDOR, only allow tokenQr changes
        if (currentUser.getRole() == Role.REPARTIDOR) {
            // Verify that ONLY tokenQr is being modified
            if (hasOtherFieldsModified(clientRequest, existing)) {
                throw new AccessDeniedException(
                        "REPARTIDOR can only assign QR codes. Use Admin account to modify client details.");
            }
        } else {
            // SUPER_ADMIN and ADMIN can modify all fields
            if (clientRequest.getFullName() != null) {
                existing.setFullName(clientRequest.getFullName());
            }
            if (clientRequest.getAddress() != null) {
                existing.setAddress(clientRequest.getAddress());
            }
            if (clientRequest.getPhoneNumber() != null) {
                existing.setPhoneNumber(clientRequest.getPhoneNumber());
            }
            if (clientRequest.getLatitude() != null) {
                existing.setLatitude(clientRequest.getLatitude());
            }
            if (clientRequest.getLongitude() != null) {
                existing.setLongitude(clientRequest.getLongitude());
            }
            if (clientRequest.getType() != null) {
                existing.setType(clientRequest.getType());
            }
            if (clientRequest.getPaymentFrequency() != null) {
                existing.setPaymentFrequency(clientRequest.getPaymentFrequency());
            }
            if (clientRequest.getCommercialStatus() != null) {
                existing.setCommercialStatus(clientRequest.getCommercialStatus());
            }
        }

        // All roles can assign TokenQR
        if (clientRequest.getTokenQr() != null && clientRequest.getTokenQr().getCode() != null) {
            com.ccalarce.siglof.model.entity.TokenQR assignedToken = tokenQRService
                    .assignTokenToClient(clientRequest.getTokenQr().getCode(), existing);
            existing.setTokenQr(assignedToken);
        }

        return repository.save(existing);
    }

    /**
     * Helper method to check if a REPARTIDOR is trying to modify fields other than
     * tokenQr
     */
    private boolean hasOtherFieldsModified(Client request, Client existing) {
        // Check if any field besides tokenQr is present in the request and different
        // from existing
        return (request.getFullName() != null && !request.getFullName().equals(existing.getFullName()))
                || (request.getAddress() != null && !request.getAddress().equals(existing.getAddress()))
                || (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(existing.getPhoneNumber()))
                || (request.getDocumentNumber() != null
                        && !request.getDocumentNumber().equals(existing.getDocumentNumber()))
                || (request.getLatitude() != null && !request.getLatitude().equals(existing.getLatitude()))
                || (request.getLongitude() != null && !request.getLongitude().equals(existing.getLongitude()))
                || (request.getType() != null && request.getType() != existing.getType())
                || (request.getPaymentFrequency() != null
                        && request.getPaymentFrequency() != existing.getPaymentFrequency())
                || (request.getCommercialStatus() != null
                        && request.getCommercialStatus() != existing.getCommercialStatus());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
