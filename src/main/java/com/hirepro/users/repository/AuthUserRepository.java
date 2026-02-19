package com.hirepro.users.repository;

import com.hirepro.users.entity.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, String> {

    @Query("SELECT u FROM AuthUser u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<AuthUser> findByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT u FROM AuthUser u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<AuthUser> findByIdAndNotDeleted(@Param("id") String id);

    @Query("SELECT u FROM AuthUser u WHERE u.clientId = :clientId AND u.deletedAt IS NULL")
    Page<AuthUser> findByClientIdAndNotDeleted(@Param("clientId") String clientId, Pageable pageable);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM AuthUser u WHERE u.clientId = :clientId AND u.deletedAt IS NULL")
    long countByClientId(@Param("clientId") String clientId);
}