package com.hirepro.clients.repository;

import com.hirepro.clients.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Client entity.
 * Extends JpaSpecificationExecutor to enable dynamic query building.
 *
 * @author HirePro Team
 * @version 1.0
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, String>, JpaSpecificationExecutor<Client> {

    /**
     * Finds a client by ID excluding soft-deleted records.
     *
     * @param id Client ID
     * @return Optional containing the client if found and not deleted
     */
    @Query("SELECT c FROM Client c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Client> findByIdAndNotDeleted(@Param("id") String id);

    /**
     * Checks if a client code already exists.
     *
     * @param clientCode Client code to check
     * @return true if exists, false otherwise
     */
    boolean existsByClientCode(String clientCode);

    /**
     * Checks if a subscription ID already exists.
     *
     * @param subscriptionId Subscription ID to check
     * @return true if exists, false otherwise
     */
    boolean existsBySubscriptionId(String subscriptionId);
}