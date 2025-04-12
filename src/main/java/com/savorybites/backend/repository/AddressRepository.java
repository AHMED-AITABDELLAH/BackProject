package com.savorybites.backend.repository;

import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.addressDetailModels.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {




    Optional<Address> findByUserAndId(LocalUser user, Long id);

    List<Address> findAllByUser(LocalUser user);


    Optional<Address> findByUserAndIsDefaultTrue(LocalUser user);

    Optional<Address> findByRestaurant(Restaurant managedAccount);
}