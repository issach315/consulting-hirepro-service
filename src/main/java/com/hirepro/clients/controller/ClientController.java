package com.hirepro.clients.controller;

import com.hirepro.clients.dto.ClientResponse;
import com.hirepro.clients.dto.CreateClientRequest;
import com.hirepro.clients.dto.UpdateClientRequest;
import com.hirepro.clients.service.ClientService;
import com.hirepro.common.dto.PageRequestDto;
import com.hirepro.common.dto.PageResponseDto;
import com.hirepro.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody CreateClientRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        ClientResponse clientResponse = clientService.createClient(request, createdBy);
        return new ResponseEntity<>(
                ApiResponse.success("Client created successfully", clientResponse),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable String clientId,
            @Valid @RequestBody UpdateClientRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String updatedBy = authentication.getName();

        ClientResponse clientResponse = clientService.updateClient(clientId, request, updatedBy);
        return ResponseEntity.ok(
                ApiResponse.success("Client updated successfully", clientResponse)
        );
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable String clientId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String deletedBy = authentication.getName();

        clientService.deleteClient(clientId, deletedBy);
        return ResponseEntity.ok(
                ApiResponse.success("Client deleted successfully")
        );
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable String clientId) {

        ClientResponse clientResponse = clientService.getClientById(clientId);
        return ResponseEntity.ok(
                ApiResponse.success("Client retrieved successfully", clientResponse)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<PageResponseDto<ClientResponse>>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Map<String, String> allParams) {

        // Create PageRequestDto with the parameters
        PageRequestDto pageRequest = new PageRequestDto();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSortBy(sortBy);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setSearch(search);

        // Extract filter parameters (all parameters that start with "filter.")
        if (allParams != null) {
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("filter.")) {
                    String filterKey = entry.getKey().substring(7); // Remove "filter." prefix
                    pageRequest.addFilter(filterKey, entry.getValue());
                }
            }
        }

        PageResponseDto<ClientResponse> clients = clientService.getAllClients(pageRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Clients retrieved successfully", clients)
        );
    }
}