package com.raiffeisen.bank.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raiffeisen.bank.DTO.ClientDTO;
import com.raiffeisen.bank.models.Client;
import com.raiffeisen.bank.repositories.ClientRepository;

@Service
public class ClientService { // simple client creation and retrieval

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public ClientDTO createClient(ClientDTO client) {
        Client newClient = Client.builder()
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .build();
        clientRepository.save(newClient);

        return mapToDTO(newClient);

    }

    public ClientDTO getClientDTOById(Long id) {
        Client client = clientRepository.findById(id).orElse(null);
        
        return client == null ? null : mapToDTO(client);
    }


    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }


    public ClientDTO mapToDTO(Client client) {
        return ClientDTO.builder()
            .id(client.getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .build();
    }

}
