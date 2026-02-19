package com.hirepro.clients.service;

import com.hirepro.clients.dto.ClientResponse;
import com.hirepro.clients.dto.CreateClientRequest;
import com.hirepro.clients.dto.UpdateClientRequest;
import com.hirepro.common.dto.PageRequestDto;
import com.hirepro.common.dto.PageResponseDto;

/**
 * Service interface for Client operations.
 * Defines business logic methods for managing clients.
 *
 * @author HirePro Team
 * @version 1.0
 */
public interface ClientService {

    /**
     * Creates a new client.
     *
     * @param request Client creation request
     * @param createdBy Username of the creator
     * @return Created client response
     */
    ClientResponse createClient(CreateClientRequest request, String createdBy);

    /**
     * Updates an existing client.
     *
     * @param clientId Client ID to update
     * @param request Client update request
     * @param updatedBy Username of the updater
     * @return Updated client response
     */
    ClientResponse updateClient(String clientId, UpdateClientRequest request, String updatedBy);

    /**
     * Soft deletes a client.
     *
     * @param clientId Client ID to delete
     * @param deletedBy Username of the deleter
     */
    void deleteClient(String clientId, String deletedBy);

    /**
     * Retrieves a client by ID.
     *
     * @param clientId Client ID
     * @return Client response
     */
    ClientResponse getClientById(String clientId);

    /**
     * Retrieves all clients with pagination, sorting, filtering, and global search.
     *
     * @param pageRequest Pagination and filter parameters
     * @return Page of client responses
     */
    PageResponseDto<ClientResponse> getAllClients(PageRequestDto pageRequest);
}