package com.savorybites.backend.repository;

import com.savorybites.backend.model.PlatformPaymentMethod;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformPaymentMethodRepository extends JpaRepository<PlatformPaymentMethod, Long> {

    Optional<PlatformPaymentMethod> findFirstByUserOrderByIdAsc(LocalUser user);

    Optional<PlatformPaymentMethod> findByIdAndUser(Long methodId, LocalUser user);

    List<PlatformPaymentMethod> findAllByUser(LocalUser user);

    boolean existsByUserAndIsDefaultTrue(LocalUser user);

    Optional<PlatformPaymentMethod> findByUserAndIsDefaultTrue(LocalUser user);

    PlatformPaymentMethod findByStripeCustomerId(String stripeCustomerId);

    boolean existsByUser(LocalUser user);
}