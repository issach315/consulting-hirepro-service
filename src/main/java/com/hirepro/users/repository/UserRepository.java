package com.hirepro.users.repository;

import com.hirepro.users.entity.User;
import com.hirepro.users.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdAndNotDeleted(String userId);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsernameAndNotDeleted(String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndNotDeleted(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllNotDeleted(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.accountStatus = :status AND u.deletedAt IS NULL")
    Page<User> findByAccountStatusAndNotDeleted(AccountStatus status, Pageable pageable);
}