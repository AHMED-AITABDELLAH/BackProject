package com.savorybites.backend.service;

import com.savorybites.backend.model.Category;
import com.savorybites.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    //Public Access
    public List<Category> getCategories(){
        return categoryRepository.findAll();
    }

    public List<Category> getCategoriesFromIDs(List<Long> listOfIDs){
        return categoryRepository.findCategoriesByIdIn(listOfIDs);
    }


    //Admin Access

    //TODO ADDING CATEGORY,DELETING AND UPDATING IT
}

