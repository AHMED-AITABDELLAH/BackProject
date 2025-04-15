package com.savorybites.backend.repository.addressDetailRepository;

import com.savorybites.backend.model.addressDetailModels.Province;
import com.savorybites.backend.model.addressDetailModels.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Optional<Province> findByIdAndRegion(Long id, Region region);

    List<Province> findAllByRegion_Id(Long regionId);
}