package com.hirepro.common.util;

import com.hirepro.clients.repository.ClientRepository;
import org.springframework.stereotype.Component;

@Component
public class ClientIdGenerator {

    private final ClientRepository clientRepository;

    public ClientIdGenerator(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public String generateClientId() {
        long count = clientRepository.count();
        long nextNumber = count + 1;
        return String.format("CLIENT%05d", nextNumber);
    }
}