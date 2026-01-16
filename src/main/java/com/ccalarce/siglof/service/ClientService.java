package com.ccalarce.siglof.service;

import com.ccalarce.siglof.model.entity.Client;
import com.ccalarce.siglof.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
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
        existing.setFullName(clientRequest.getFullName());
        existing.setAddress(clientRequest.getAddress());
        existing.setPhoneNumber(clientRequest.getPhoneNumber());
        existing.setLatitude(clientRequest.getLatitude());
        existing.setLongitude(clientRequest.getLongitude());

        // Allow assigning a TokenQR during update (e.g. initial deployment)
        if (clientRequest.getTokenQr() != null && clientRequest.getTokenQr().getCode() != null) {
            com.ccalarce.siglof.model.entity.TokenQR assignedToken = tokenQRService
                    .assignTokenToClient(clientRequest.getTokenQr().getCode(), existing);
            existing.setTokenQr(assignedToken);
        }

        // Document Number usually shouldn't change, but depends on rules. Keeping it
        // safe for now.
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
