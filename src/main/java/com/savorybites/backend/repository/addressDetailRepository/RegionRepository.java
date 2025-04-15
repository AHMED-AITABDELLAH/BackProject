package com.savorybites.backend.repository.addressDetailRepository;

import com.savorybites.backend.model.addressDetailModels.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

}