package com.hirepro.clients.service;

import com.hirepro.clients.dto.ClientResponse;
import com.hirepro.clients.dto.CreateClientRequest;
import com.hirepro.clients.dto.UpdateClientRequest;
import com.hirepro.clients.entity.Client;
import com.hirepro.clients.repository.ClientRepository;
import com.hirepro.common.dto.PageRequestDto;
import com.hirepro.common.dto.PageResponseDto;
import com.hirepro.common.exception.BadRequestException;
import com.hirepro.common.exception.ResourceNotFoundException;
import com.hirepro.common.util.PageMapper;
import com.hirepro.common.util.SpecificationBuilder;
import com.hirepro.common.util.UlidGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of ClientService interface.
 * Handles all business logic related to client operations.
 *
 * @author HirePro Team
 * @version 1.0
 */
@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    // Fields that can be searched globally
    private static final String[] SEARCHABLE_FIELDS = {"name", "clientCode", "regions", "status"};

    public ClientServiceImpl(ClientRepository clientRepository, ModelMapper modelMapper) {
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public ClientResponse createClient(CreateClientRequest request, String createdBy) {

        // Check if client code already exists
        if (clientRepository.existsByClientCode(request.getClientCode())) {
            throw new BadRequestException("Client code already exists");
        }

        // Check if subscription ID already exists
        if (request.getSubscriptionId() != null &&
                clientRepository.existsBySubscriptionId(request.getSubscriptionId())) {
            throw new BadRequestException("Subscription ID already exists");
        }

        Client client = modelMapper.map(request, Client.class);

        // Generate ULID for client ID
        client.setId(UlidGenerator.generate());
        client.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        client.setCreatedBy(createdBy);

        Client savedClient = clientRepository.save(client);
        return modelMapper.map(savedClient, ClientResponse.class);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(String clientId, UpdateClientRequest request, String updatedBy) {

        Client client = clientRepository.findByIdAndNotDeleted(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // Check if client code already exists (if being updated)
        if (request.getClientCode() != null &&
                !request.getClientCode().equals(client.getClientCode()) &&
                clientRepository.existsByClientCode(request.getClientCode())) {
            throw new BadRequestException("Client code already exists");
        }

        // Check if subscription ID already exists (if being updated)
        if (request.getSubscriptionId() != null &&
                !request.getSubscriptionId().equals(client.getSubscriptionId()) &&
                clientRepository.existsBySubscriptionId(request.getSubscriptionId())) {
            throw new BadRequestException("Subscription ID already exists");
        }

        modelMapper.map(request, client);
        client.setUpdatedBy(updatedBy);

        Client updatedClient = clientRepository.save(client);
        return modelMapper.map(updatedClient, ClientResponse.class);
    }

    @Override
    @Transactional
    public void deleteClient(String clientId, String deletedBy) {

        Client client = clientRepository.findByIdAndNotDeleted(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        client.setDeletedAt(LocalDateTime.now());
        client.setUpdatedBy(deletedBy);

        clientRepository.save(client);
    }

    @Override
    public ClientResponse getClientById(String clientId) {

        Client client = clientRepository.findByIdAndNotDeleted(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        return modelMapper.map(client, ClientResponse.class);
    }

    @Override
    public PageResponseDto<ClientResponse> getAllClients(PageRequestDto pageRequest) {

        // Build the specification with filters
        Specification<Client> spec = buildSpecification(pageRequest);

        // Create pageable with sorting
        Pageable pageable = createPageable(pageRequest);

        // Execute query
        Page<Client> clientPage = clientRepository.findAll(spec, pageable);

        // Convert to PageResponseDto
        return PageMapper.toPageResponse(clientPage, modelMapper, ClientResponse.class);
    }

    /**
     * Builds a JPA Specification based on the page request parameters.
     *
     * @param pageRequest Page request with filters and search
     * @return Combined specification
     */
    private Specification<Client> buildSpecification(PageRequestDto pageRequest) {
        Specification<Client> spec = SpecificationBuilder.isNotDeleted();

        // Add global search if present
        if (pageRequest.getSearch() != null && !pageRequest.getSearch().trim().isEmpty()) {
            Specification<Client> searchSpec = SpecificationBuilder.globalSearch(
                    pageRequest.getSearch(),
                    SEARCHABLE_FIELDS
            );
            spec = spec.and(searchSpec);
        }

        // Add filters if present
        if (pageRequest.getFilters() != null && !pageRequest.getFilters().isEmpty()) {
            Specification<Client> filterSpec = SpecificationBuilder.withFilters(pageRequest.getFilters());
            spec = spec.and(filterSpec);
        }

        return spec;
    }

    /**
     * Creates a Pageable object based on page request parameters.
     *
     * @param pageRequest Page request with pagination and sorting
     * @return Pageable object
     */
    private Pageable createPageable(PageRequestDto pageRequest) {
        Sort sort = Sort.unsorted();

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "ASC".equalsIgnoreCase(pageRequest.getSortDirection())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sort = Sort.by(direction, pageRequest.getSortBy());
        }

        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }
}