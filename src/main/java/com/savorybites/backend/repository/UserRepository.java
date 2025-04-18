package com.savorybites.backend.repository;

import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<LocalUser, Long> {


    Optional<LocalUser> findByEmailIgnoreCase(String email);

    Optional<LocalUser> findLocalUserById(Long id);

    long countByStatus(Status status);
}
