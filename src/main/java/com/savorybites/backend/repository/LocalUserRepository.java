package com.savorybites.backend.repository;

import com.savorybites.backend.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface LocalUserRepository extends JpaRepository<LocalUser, UUID> {


    Optional<LocalUser> findByEmailIgnoreCase(String email);
}
