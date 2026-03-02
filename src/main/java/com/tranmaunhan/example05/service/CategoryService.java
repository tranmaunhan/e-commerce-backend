package com.tranmaunhan.example05.service;


import com.tranmaunhan.example05.entities.Category;
import com.tranmaunhan.example05.payloads.CategoryDTO;
import com.tranmaunhan.example05.payloads.CategoryResponse;

public interface CategoryService {

    CategoryDTO createCategory(Category category);

    CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO getCategoryById(Long categoryId);
    CategoryDTO updateCategory(Category category, Long categoryId);

    String deleteCategory(Long categoryId);
}