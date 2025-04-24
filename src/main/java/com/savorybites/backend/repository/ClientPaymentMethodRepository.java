package com.savorybites.backend.repository;

import com.savorybites.backend.model.ClientPaymentMethod;
import com.savorybites.backend.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientPaymentMethodRepository extends JpaRepository<ClientPaymentMethod, Long> {
    boolean existsByIsDefaultTrue();

    Optional<ClientPaymentMethod> findFirstByUserOrderByIdAsc(LocalUser user);

    Optional<ClientPaymentMethod> findByIdAndUser(Long methodId, LocalUser user);

    void clearAllDefaultsForUser(Long id);

    List<ClientPaymentMethod> findAllByUser(LocalUser user);

    boolean existsByUserAndIsDefaultTrue(LocalUser user);
}