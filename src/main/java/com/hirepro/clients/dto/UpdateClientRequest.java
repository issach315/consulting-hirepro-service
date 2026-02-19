package com.hirepro.clients.dto;

import jakarta.validation.constraints.Pattern;

public class UpdateClientRequest {

    private String clientCode;

    private String name;

    @Pattern(regexp = "DOMESTIC|USIT|BOTH", message = "Regions must be DOMESTIC, USIT, or BOTH")
    private String regions;

    private String subscriptionId;

    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
    private String status;

    // Constructors
    public UpdateClientRequest() {
    }

    // Getters and Setters
    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}