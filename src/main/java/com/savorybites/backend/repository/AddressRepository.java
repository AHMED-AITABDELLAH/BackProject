package com.savorybites.backend.repository;

import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {




    Optional<Address> findByUserAndId(LocalUser user, Long id);

    List<Address> findAllByUser(LocalUser user);

    List<Address> findAllByUserAndId(LocalUser user, Long id);

    Optional<Address> findByUserAndIsDefaultTrue(LocalUser user);
}