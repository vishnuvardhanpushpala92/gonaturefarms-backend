package com.gonaturefarms.controller;

import com.gonaturefarms.dto.admin.CategoryRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Admin category management (/api/admin/categories). */
@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ApiResponse create(@RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @DeleteMapping("/{name}")
    public ApiResponse delete(@PathVariable String name) {
        return categoryService.delete(name);
    }
}
