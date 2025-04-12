package com.savorybites.backend.repository.addressDetailRepository;

import com.savorybites.backend.model.addressDetailModels.Commune;
import com.savorybites.backend.model.addressDetailModels.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommuneRepository extends JpaRepository<Commune, Long> {
    Optional<Commune> findByIdAndProvince(Long id, Province province);
}