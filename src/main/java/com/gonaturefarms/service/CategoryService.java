package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.CategoryRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Category;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        List<Category> allCategories = categoryRepository.findAllByOrderByNameAsc();
        // Filter out pending categories for public view
        List<Category> activeCategories = allCategories.stream()
                .filter(category -> category.getPending() == null || !category.getPending())
                .collect(Collectors.toList());
        return ApiResponse.ok().with("categories", activeCategories);
    }

    @Transactional(readOnly = true)
    public ApiResponse listAll() {
        List<Category> allCategories = categoryRepository.findAllByOrderByNameAsc();
        // Include pending categories for admin view
        return ApiResponse.ok().with("categories", allCategories);
    }

    @Transactional
    public ApiResponse create(CategoryRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ApiException("Category name required");
        }
        String name = req.getName().trim();
        // INSERT IGNORE semantics
        if (categoryRepository.findByName(name).isEmpty()) {
            try {
                categoryRepository.save(Category.builder().name(name).pending(true).build());
            } catch (DataIntegrityViolationException ignored) {
                // already exists, treat as success
            }
        }
        return ApiResponse.ok("Category added");
    }

    @Transactional
    public ApiResponse delete(String name) {
        categoryRepository.deleteByName(name);
        return ApiResponse.ok("Category deleted");
    }
}
