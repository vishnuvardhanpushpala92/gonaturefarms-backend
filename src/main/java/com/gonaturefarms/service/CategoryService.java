package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.CategoryRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Category;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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
                categoryRepository.save(new Category(null, name));
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
