package com.savorybites.backend.repository.addressDetailRepository;

import com.savorybites.backend.model.addressDetailModels.Commune;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommuneRepository extends JpaRepository<Commune, Long> {
}