package com.savorybites.backend.repository;

import com.savorybites.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {


    List<Category> findCategoriesByIdIn(Collection<Long> ids);
}