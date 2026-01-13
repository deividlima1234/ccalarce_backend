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

    public List<Client> findAll() {
        return repository.findAll();
    }

    public Client findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public Client save(Client client) {
        return repository.save(client);
    }

    public Client update(Long id, Client clientRequest) {
        Client existing = findById(id);
        existing.setFullName(clientRequest.getFullName());
        existing.setAddress(clientRequest.getAddress());
        existing.setPhoneNumber(clientRequest.getPhoneNumber());
        existing.setLatitude(clientRequest.getLatitude());
        existing.setLongitude(clientRequest.getLongitude());
        // Document Number usually shouldn't change, but depends on rules. Keeping it
        // safe for now.
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
