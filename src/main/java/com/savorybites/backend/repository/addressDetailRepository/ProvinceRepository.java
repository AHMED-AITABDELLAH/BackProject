package com.savorybites.backend.repository.addressDetailRepository;

import com.savorybites.backend.model.addressDetailModels.Province;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
}